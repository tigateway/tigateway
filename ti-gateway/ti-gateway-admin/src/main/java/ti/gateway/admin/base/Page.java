package ti.gateway.admin.base;

import java.util.List;

/**
 * 分页工具类
 */
public class Page<T> {

    /**
     * @Fields currentPage : 当前页面
     */
    private int pageNo;

    /**
     * @Fields pageSize : 当前页面拥有条目数
     */
    private int pageSize;

    /**
     * @Fields pageCount : 分页数量
     */
    private int totalPage;

    /**
     * @Fields total : TODO(数据总条目数)
     */
    private int totalRecord;
    /**
     * @Fields resultList : 返回的数据载体
     */
    private List<T> list;

    /**
     * <p>
     * Title: 构造方法1
     * </p>
     * <p>
     * Description:
     * </p>
     *
     */
    public Page() {

    }

    /**
     * <p>
     * Title: 构造方法2
     * </p>
     * <p>
     * Description:
     * </p>
     *
     * @param sum  总条目数
     * @param size 当前页面数量
     */
    public Page(int sum, int page, int size) {
        pageSize = size;
        totalRecord = sum;
        pageNo = page;
        init();
    }

    private void init() {
        totalPage = totalRecord % pageSize == 0 ? totalRecord / pageSize : totalRecord / pageSize
                + 1;
        // check page
        pageNo = pageNo < 1 ? 1
                : ((pageNo > totalPage) ? totalPage : pageNo);
    }

    /**
     * @param @return 设定文件
     * @return int 返回类型
     * @throws
     * @Title: getStart
     * @Description: 得到start
     * @date 2012-03-20 16:23:09 +0800
     */
    public int getStart() {
        return 0 != pageNo ? (pageNo - 1) * pageSize : 0;
    }

    /**
     * @return the currentPage
     */
    public int getCurrentPage() {
        return pageNo;
    }

    /**
     * @param currentPage the currentPage to set
     */
    public void setCurrentPage(int currentPage) {
        this.pageNo = currentPage;
    }

    /**
     * @return the pageSize
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * @param pageSize the pageSize to set
     */
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    /**
     * @return the totalCount
     */
    public int getTotalCount() {
        return totalRecord;
    }

    /**
     *
     */
    public void setTotalCount(int total) {
        this.totalRecord = total;
    }

    /**
     * @return the pageCount
     */
    public int getPageCount() {
        return totalPage;
    }

    /**
     * @param pageCount the pageCount to set
     */
    public void setPageCount(int pageCount) {
        this.totalPage = pageCount;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    public int getTotal() {
        return totalRecord;
    }

    public void setTotal(int total) {
        this.totalRecord = total;
    }

}
