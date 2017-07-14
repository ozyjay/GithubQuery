class RepoRecord {
    String owner;
    String name;
    String creationDate;
    int contributors;
    int forks;
    int stars;
    int watchers;
    int releases;
    int openIssues;
    int closedIssues;
    int openPulls;
    int closedPulls;

    @Override
    public String toString() {
        return String.format("%s,%s,%s,%d,%d,%d,%d,%d,%d,%d,%d,%d",
                name, owner, creationDate, watchers, forks, stars,
                contributors, releases, openIssues, closedIssues, openPulls, closedPulls);
    }
}