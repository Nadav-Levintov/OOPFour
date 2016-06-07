package OOP.Solution;

import OOP.Provided.*;
import javafx.util.Pair;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;


public class OOPMultipleControl {

    //TODO: DO NOT CHANGE !!!!!!
    private Class<?> interfaceClass;
    private File sourceFile;

    //TODO: DO NOT CHANGE !!!!!!
    public OOPMultipleControl(Class<?> interfaceClass, File sourceFile) {
        this.interfaceClass = interfaceClass;
        this.sourceFile = sourceFile;
    }

    //TODO: fill in here :
    public void validateInheritanceTree() throws OOPMultipleException {
        validateAux(interfaceClass);
    }

    //TODO: fill in here :
    public Object invoke(String methodName, Object[] args)
            throws OOPMultipleException {
        Method[] methods = interfaceClass.getMethods();
        List<Method> compatiableMethods = new LinkedList<Method>();
        int argsLength = 0;
        if (args != null) {
            argsLength = args.length;
        }
        Class[] paramTypes = new Class<?>[argsLength];
        for (int i = 0; i < argsLength; i++) {
            paramTypes[i] = args[i].getClass();
        }
        List<Method> sameNameMethods = new LinkedList<Method>();
        for (Method aMethod : methods
                ) {
            if (aMethod.getName().equals(methodName)) {
                sameNameMethods.add(aMethod);
            }
        }
        if(sameNameMethods.size()>1)
        {
            List<Pair<Class<?>, Method>> clashingSet = new LinkedList<Pair<Class<?>, Method>>();
            for (Method aMethod : sameNameMethods) {
                Pair<Class<?>, Method> pair = new Pair<Class<?>, Method>(aMethod.getDeclaringClass(), aMethod);
                clashingSet.add(pair);
            }
            throw new OOPCoincidentalAmbiguity(clashingSet);
        }

        for (Method aMethod : methods
                ) {
            if (aMethod.getName().equals(methodName) &&
                    Arrays.equals(paramTypes, aMethod.getParameterTypes())) {
                compatiableMethods.add(aMethod);
            }
        }

        for (Method aMethod:compatiableMethods) {
            if(aMethod.getAnnotation(OOPMethod.class).modifier().equals(OOPModifier.DEFAULT))
            {
                Package pkg = aMethod.getDeclaringClass().getPackage();
                if(!checkDefaultPackage(pkg,aMethod,interfaceClass))
                {
                    compatiableMethods.remove(aMethod);
                }
            }

        }
        if (compatiableMethods.size() == 0) {
            for (Method aMethod : methods
                    ) {
                if (aMethod.getName().equals(methodName) &&
                        typeArrayCheck(aMethod.getParameterTypes(), paramTypes)) {
                    compatiableMethods.add(aMethod);
                }
            }
            return invokeAux(compatiableMethods, args);

        }
        return invokeAux(compatiableMethods, args);


    }

    //TODO: add more of your code :
    public Object invokeAux(List<Method> compatiableMethods, Object[] args) throws OOPMultipleException {
        if (compatiableMethods.size() == 1) {
            Method theMethod = compatiableMethods.get(0);
            OOPModifier methodMod = theMethod.getAnnotation(OOPMethod.class).modifier();
            if (methodMod.equals(OOPModifier.PRIVATE)) {
                throw new OOPInaccessibleMethod();
            }
            if (methodMod.equals(OOPModifier.DEFAULT)) {
                Package pkg = theMethod.getDeclaringClass().getPackage();
                if (checkPackage(interfaceClass, pkg, theMethod.getDeclaringClass())) {
                    throw new OOPInaccessibleMethod();
                }

            }

            String interfaceName = theMethod.getDeclaringClass().getName();
            int lastDot = interfaceName.lastIndexOf('.');
            String clsName = interfaceName.substring(0, lastDot + 1) + "C" + interfaceName.substring(lastDot + 2, interfaceName.length());
            Class<?> cls;
            try {
                cls = Class.forName(clsName);
            } catch (ClassNotFoundException e) {
                throw new OOPInaccessibleMethod();
            }
            Constructor<?> constr;
            try {
                constr = cls.getConstructor();
            } catch (NoSuchMethodException e) {
                throw new OOPInaccessibleMethod();
            }
            Object obj;
            try {
                obj = constr.newInstance(new Object[]{});
            } catch (Exception e) {
                throw new OOPInaccessibleMethod();
            }
            Object retVal;
            if(theMethod.getAnnotation(OOPMethod.class).modifier().equals(OOPModifier.DEFAULT))
            {
                Package declaringPkg = theMethod.getDeclaringClass().getPackage();
                if(!checkDefaultPackage(declaringPkg,theMethod,interfaceClass))
                {
                   throw new OOPInaccessibleMethod();
                }
            }
            try {
                retVal = theMethod.invoke(obj, args);
            } catch (Exception e) {
                throw new OOPInaccessibleMethod();
            }
            return retVal;
        } else {
            if (compatiableMethods.size() == 0) {
                throw new OOPInaccessibleMethod();
            }


            List<Pair<Class<?>, Method>> clashingSet = new LinkedList<Pair<Class<?>, Method>>();
            for (Method aMethod : compatiableMethods) {
                Pair<Class<?>, Method> pair = new Pair<Class<?>, Method>(aMethod.getDeclaringClass(), aMethod);
                clashingSet.add(pair);
            }
            throw new OOPCoincidentalAmbiguity(clashingSet);
        }
    }
    public Boolean checkDefaultPackage(Package pkg, Method aMethod, Class<?> current)
    {
        if(!current.getPackage().equals(pkg))
        {
            return false;
        }

        Class<?>[] parentInterfaces = current.getInterfaces();
        if(current.equals(aMethod.getDeclaringClass()))
        {
            return true;
        }

        List<Class<?>> potential = new LinkedList<>();
        for (int i=0;i<parentInterfaces.length;i++)
        {
            if (aMethod.getDeclaringClass().isAssignableFrom(parentInterfaces[i]))
            {
                potential.add(parentInterfaces[i]);
            }
        }
        Boolean retVal = false;
        if(potential.size()>0)
        {
            for (Class<?> inter:potential ) {
                if(!retVal && checkDefaultPackage(pkg,aMethod,inter))
                {
                    retVal=true;
                }

            }
        }
        return retVal;
    }
    public Boolean typeArrayCheck(Class<?>[] currentMArray, Class<?>[] testedMArray) {
        if (currentMArray.length != testedMArray.length) {
            return false;
        }

        int size = currentMArray.length;
        for (int i = 0; i < size; i++) {
            if (!currentMArray[i].isAssignableFrom(testedMArray[i])) {
                return false;
            }
        }
        return true;

    }

    public Boolean checkPackage(Class<?> cls, Package pkg, Class<?> parent) {
        if (cls.getPackage().equals(pkg)) {
            List<Class<?>> methodsClass = new LinkedList<Class<?>>();
            Class<?>[] parents = cls.getInterfaces();
            for (Class cl : parents) {
                if (parent.isAssignableFrom(cl)) {
                    return checkPackage(cl, pkg, parent);
                }
            }
        }
        return false;
    }

    public List<Method> validateAux(Class<?> current) throws OOPMultipleException {
        Class[] hierarchyTree = current.getInterfaces();
        /*check if this is the highest level in the current branch*/
        if (hierarchyTree.length == 0) {
            List<Method> methods = new LinkedList<Method>();
            Method[] methodArr = current.getMethods();
            for (Method aMethod : methodArr) {
                checkAnnotations(aMethod);
                methods.add(aMethod);
            }
            return methods;
        }

        List<Method> methodList = validateAux(hierarchyTree[0]);
    /*check for functions declared in all of the parent levels*/
        for (int i = 1; i < hierarchyTree.length; i++) {
            List<Method> currentMethods = validateAux(hierarchyTree[i]);
            for (Method currentMethod : currentMethods) {
                for (Method existingMethod : methodList) {
                    if (methodCompare(existingMethod, currentMethod)) {
                        checkInharent(existingMethod, currentMethod, current);
                        checkAnnotationsPermission(existingMethod, currentMethod);
                    }

                }
            }
            methodList.addAll(currentMethods);
        }

        /*check for functions declared in current level*/
        Method[] currentMethods = current.getDeclaredMethods();
        for (Method currentMethod : currentMethods) {
            for (Method existingMethod : methodList) {
                if (methodCompare(existingMethod, currentMethod)) {
                    checkInharent(existingMethod, currentMethod, current);
                    checkAnnotationsPermission(existingMethod, currentMethod);
                }
            }
        }
        methodList.addAll(Arrays.asList(currentMethods));


        return methodList;
    }

    public void checkAnnotations(Method aMethod) throws OOPBadClass {
        if (!aMethod.isAnnotationPresent(OOPMethod.class)) {

            throw new OOPBadClass(aMethod);
        }
    }

    public void checkAnnotationsPermission(Method existingMethod, Method currentMethod) throws OOPBadClass {
        if (existingMethod.getDeclaringClass().isAssignableFrom(currentMethod.getDeclaringClass())) {
            OOPMethod currentA = currentMethod.getAnnotation(OOPMethod.class);
            OOPMethod existingA = existingMethod.getAnnotation(OOPMethod.class);
            if (/*Modifier.isFinal(existingMethod.getModifiers()) ||*/ currentA.modifier().ordinal() < existingA.modifier().ordinal()) {
                throw new OOPBadClass(currentMethod);
            }
        }
    }

    public void checkInharent(Method existingMethod, Method currentMethod, Class<?> current) throws OOPInherentAmbiguity {
        try {
            Method curLevelMethod = current.getDeclaredMethod(existingMethod.getName(), existingMethod.getParameterTypes());
        } catch (NoSuchMethodException e) {
            if (currentMethod.getDeclaringClass().equals(existingMethod.getDeclaringClass())) {
                throw new OOPInherentAmbiguity(interfaceClass, currentMethod.getDeclaringClass(), currentMethod);
            }
        }
    }

    public Boolean methodCompare(Method m1, Method m2) {
        return m1.getName().equals(m2.getName()) &&
                Arrays.equals(m1.getParameterTypes(), m2.getParameterTypes())
               /*&& m1.getReturnType().equals(m2.getReturnType())*/;
    }
    //TODO: DO NOT CHANGE !!!!!!

    public void removeSourceFile() {
        if (sourceFile.exists()) {
            sourceFile.delete();
        }
    }
}
