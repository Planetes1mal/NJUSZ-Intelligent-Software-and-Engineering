package lab3;

public class Student {
    public String name;
    protected int id;
    private List<Course> courses;

    public void addCourse(Course course) {
        courses.add(course);
    } 
}

public class Course {
    public String name;
    protected int credits;

    public void updateCredits(int credits) {
        this.credits = credits;
    }
}
