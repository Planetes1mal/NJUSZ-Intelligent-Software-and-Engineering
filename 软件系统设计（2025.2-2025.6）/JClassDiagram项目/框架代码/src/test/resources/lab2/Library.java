package lab2;

public class Library {
    Map<Integer, Book> books;

    Book getBook(int id) {
        return books.get(id);
    }
}

public interface Readable {
    String getContent();
}

class People {
    Readable[] things;
}

class Book implements Readable {
    String content;

    @Override
    public String getContent() {
        return content;
    }
}
