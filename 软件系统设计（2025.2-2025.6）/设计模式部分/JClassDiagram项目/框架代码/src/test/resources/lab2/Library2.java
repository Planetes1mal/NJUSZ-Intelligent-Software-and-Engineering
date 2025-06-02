package lab2;

public class Library2 {
    public Map<Student, List<Book>> records;

    private ArrayList<Manager> managers;

    protected Library2 friendLibrary;
}

public class Student {
    public String name;
    private int age;
}

public class Book {
    Page[][] pages;
    private Integer price;

    public int getPrice() {
        return price;
    }
}

public class Page {
    public String content;
}

public class Manager {
    // it's an empty class
}