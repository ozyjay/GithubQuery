class Record {
    String owner;
    String committer;
    String date;
    String message;
    int additions;
    int deletions;
    String filenames;

    @Override
    public String toString() {
        return String.format("%s,%s,%s,%s,%d,%d,%s",
                date, committer, owner, message, additions, deletions, filenames);
    }
}