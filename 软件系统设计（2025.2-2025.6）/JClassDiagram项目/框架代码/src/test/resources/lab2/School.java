package lab2;

public class School {
    public Map<Grade, List<Map<Group, List<Student>>>> table;

    public void addStudent(Student student) {
        // do something
    }
}

public class Grade {
    public int level;
}

public class Group {
    public void addStudent(Student student) {
        if (student.age < 18) {
            return;
        }
        // do something
    }

    private void removeStudent(Student student) {
        Student st = student;
    }
}

public class Student {
    private String name;
    public int age;
}

public class SmartStudent extends Student {
    public void study() {
        // do something
    }
}