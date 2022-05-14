package com.linsh.register;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

/**
 * <pre>
 *    author : Senh Linsh
 *    github : https://github.com/SenhLinsh
 *    date   : 2021/05/18
 *    desc   : 接口注册处理器
 *
 *             对使用 {@link InterfaceRegister} 进行接口注册的类进行实际实际代码注册处理。
 *
 *             注意：
 *                 1. 对于多模块代码，编译时将在每个模块都会单独初始化 RegisterProcessor 并进行单独处理，自动
 *                    生成的 java 代码也会在对应的模块下面，需要避免生成相同的类名而出现的类名冲突的问题。一旦
 *                    类名冲突时，编译器将会默认使用 app 模块的类来使用，将会丢失其他子模块自动生成的代码信息。
 * </pre>
 */
@AutoService(Processor.class)
public class RegisterProcessor extends AbstractProcessor {

    private static final String REGISTER_PACKAGE = "com.linsh.register";
    private static final String REGISTER_CLASS_NAME = "_InterfaceRegisters";
    // interface : implements
    private final Map<String, List<String>> registers = new HashMap<>();
    private char postFixIndex = 'A';
    private Filer filer;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        log("init");
        filer = processingEnv.getFiler();

        Elements elementUtils = processingEnv.getElementUtils();
        while (elementUtils.getTypeElement(REGISTER_PACKAGE + "." + REGISTER_CLASS_NAME + postFixIndex) != null) {
            postFixIndex++;
        }
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        log("getSupportedSourceVersion");
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        log("getSupportedAnnotationTypes");
        return ImmutableSet.of(InterfaceRegister.class.getName());
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        log("process");
        // 遍历接口注册
        for (Element element : roundEnv.getElementsAnnotatedWith(InterfaceRegister.class)) {
            if (element.getKind() == ElementKind.CLASS) {
                handleClass((TypeElement) element);
            }
        }
        if (!roundEnv.processingOver()) {
            return false;
        }
        // 将注册信息写入到 Class 中
        generateClassFile();
        return false;
    }

    private void handleClass(TypeElement element) {
        log("handle class: " + element.getQualifiedName().toString());
        List<String> registerInterfaces = getRegisterInterfaces(element);
        log("register interfaces: " + registerInterfaces);
        for (String registerInterface : registerInterfaces) {
            List<String> curImplements = registers.get(registerInterface);
            if (curImplements == null) {
                curImplements = new ArrayList<>();
                registers.put(registerInterface, curImplements);
            }
            if (element.getModifiers().contains(Modifier.PUBLIC)) {
                curImplements.add(element.getQualifiedName().toString() + ".class");
            } else {
                // 对于内部类的反射，需要使用 $
                String qualifiedName = element.getQualifiedName().toString();
                String packageName = processingEnv.getElementUtils().getPackageOf(element).toString();
                String className = packageName + "." + qualifiedName.substring(packageName.length() + 1).replaceAll("\\.", "\\$");
                log("add: " + className);
                curImplements.add(className);
            }
        }
    }

    /**
     * 获取当前类注册的接口
     *
     * @param element 当前类
     * @return 注册的所有接口
     */
    private List<String> getRegisterInterfaces(TypeElement element) {
        List<String> registerInterfaces = new ArrayList<>();
        List<? extends AnnotationMirror> mirrors = element.getAnnotationMirrors();
        for (AnnotationMirror mirror : mirrors) {
            if (mirror.getAnnotationType().toString().equals(InterfaceRegister.class.getCanonicalName())) {
                AnnotationValue value = mirror.getElementValues().values().iterator().next();
                registerInterfaces.add(value.toString());
            }
        }
        return registerInterfaces;
    }

    private void generateClassFile() {
        // 私有化构造器
        MethodSpec constructor = MethodSpec.constructorBuilder().addModifiers(Modifier.PRIVATE).build();
        // 静态方法：获取实现类
        MethodSpec.Builder initMethodBuilder = MethodSpec.methodBuilder("init")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addException(ClassNotFoundException.class);
        for (Map.Entry<String, List<String>> entry : registers.entrySet()) {
            StringBuilder builder = new StringBuilder();
            for (String impl : entry.getValue()) {
                if (builder.length() == 0) {
                    builder.append("new Class[]{");
                } else {
                    builder.append(", ");
                }
                if (impl.endsWith(".class")) {
                    builder.append(impl);
                } else {
                    // 非 .class 结尾，即为非 public 的类，需要使用反射获取 class
                    builder.append("java.lang.Class.forName(\"").append(impl).append("\")");
                }
            }
            if (builder.length() > 0) {
                builder.append("}");
            }
            String impls = builder.toString();
            log("put impls, " + entry.getKey() + " : " + impls);
            initMethodBuilder.addStatement("com.linsh.register.InterfaceRegisters.register($L, $L)", entry.getKey(), impls);
        }
        MethodSpec initMethod = initMethodBuilder
                .returns(void.class)
                .build();
        // InterfaceRegisters 类
        String className = REGISTER_CLASS_NAME + postFixIndex;
        TypeSpec typeSpec = TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(constructor)
                .addMethod(initMethod)
                .build();
        // Java 文件
        JavaFile javaFile = JavaFile.builder(REGISTER_PACKAGE, typeSpec).build();
        try {
            log("write java file: " + className);
            javaFile.writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void log(String msg) {
        System.out.println("RegisterProcessor: " + msg);
    }
}