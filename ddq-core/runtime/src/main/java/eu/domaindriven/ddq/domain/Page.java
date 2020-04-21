package eu.domaindriven.ddq.domain;

import java.util.*;

public class Page<T extends Entity> {

    private final List<T> entries;
    private final int pageIndex;
    private final int pageCount;
    private final int pageSize;

    public Page(List<T> entries, int pageIndex, int pageCount, int pageSize) {
        this.entries = new ArrayList<>(entries);
        this.pageIndex = pageIndex;
        this.pageCount = pageCount;
        this.pageSize = pageSize;
    }

    public List<T> entries() {
        return Collections.unmodifiableList(entries);
    }

    public int count() {
        return entries.size();
    }

    public boolean hasPrevious() {
        return pageIndex > 0;
    }

    public boolean hasNext() {
        return pageIndex < pageCount - 1;
    }

    public int pageIndex() {
        return pageIndex;
    }

    public int nextPageIndex() {
        if (!hasNext()) {
            throw new IllegalStateException("This page has no next index");
        }
        return pageIndex + 1;
    }

    public int previousPageIndex() {
        if (!hasPrevious()) {
            throw new IllegalStateException("This page has no previous index");
        }
        return pageIndex - 1;
    }

    public int pageCount() {
        return pageCount;
    }

    public int pageSize() {
        return pageSize;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Page<?> page = (Page<?>) o;
        return pageIndex == page.pageIndex &&
                pageCount == page.pageCount &&
                pageSize == page.pageSize &&
                entries.equals(page.entries);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entries, pageIndex, pageCount, pageSize);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Page.class.getSimpleName() + "[", "]")
                .add("entries=" + entries)
                .add("pageIndex=" + pageIndex)
                .add("pageCount=" + pageCount)
                .add("pageSize=" + pageSize)
                .toString();
    }
}
