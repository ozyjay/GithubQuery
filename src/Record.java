class Record {
    String owner;
    String committer;
    String date;
    String message;
//    int comments;
    int additions;
    int deletions;
    int changes;

    @Override
    public String toString() {
        return String.format("%s,%s,%s,%s,%d,%d,%d",
                date, committer, owner, message, additions, deletions, changes);
    }
}