package org.mybatis.example;

/**
 * @author haishen
 * @date 2019/7/31
 */

/**
 * 分页查询对象，走分页拦截器时使用
 */
public class PageCondition {

    private int totalCount;
    private int totalPage;
    private int currentPage;
    private int pageSize;

    public PageCondition() {

    }

    public PageCondition(int currentPage, int pageSize) {
        this.currentPage = transferCurrentPage(currentPage);
        this.pageSize = pageSize;
    }

    /**
     * 转换当前页，页面当前页1开始，sql当前页0开始
     */
    private int transferCurrentPage(int currentPage) {
        if (currentPage > 0) {
            currentPage -= 1;
        }
        return currentPage;
    }

    /**
     * 恢复currentPage
     */
    public void recoveryCurrentPage() {
        currentPage += 1;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
        this.totalPage = this.totalCount % this.pageSize == 0 ? this.totalCount / this.pageSize : this.totalCount / this.pageSize + 1;
    }

    public int getTotalPage() {
        return totalPage;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = transferCurrentPage(currentPage);
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public void setTotalPage(int totalPage) {
        this.totalPage = totalPage;
    }

    @Override
    public String toString() {
        return "PageCondition{" +
                "totalCount=" + totalCount +
                ", totalPage=" + totalPage +
                ", currentPage=" + currentPage +
                ", pageSize=" + pageSize +
                '}';
    }
}
