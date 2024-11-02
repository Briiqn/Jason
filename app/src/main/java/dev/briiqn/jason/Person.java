package dev.briiqn.jason;

import dev.briiqn.jason.interpreter.Interpreter;

@Interpreter.ScriptType(name = "Person")
public class Person {
    private String name;
    private int age;

    @Interpreter.ScriptConstructor(name="withParams")
    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }
    @Interpreter.ScriptConstructor(name="default")
    public Person() {
        this.name="joe";
        this.age=80;
    }



    @Interpreter.ScriptFunction(name = "getName")
    public String getName() {
        return name;
    }

    @Interpreter.ScriptFunction(name = "getAge")
    public int getAge() {
        return age;
    }
    @Interpreter.ScriptFunction(name = "greet")

    public void greet(){
        System.out.println("Greetings, im  "+name +" and i am  "+age);
    }

    @Interpreter.ScriptFunction(name = "toString")
    public String toString() {
        return "Person{name='" + name + "', age=" + age + "}";
    }
}