package uz.uzcard.genesis.dto;


import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Virus on 28-Aug-16.
 */
public class FilterParameters extends AbstractMap implements Serializable {

    public static final String STATUS = "status";
    public static final String USER_ID = "user_id";
    public static final String STUDENT_ID = "student_id";
    public static final String DATE = "DATE";
    public static final String FROM_DATE = "FROM_DATE";
    public static final String TO_DATE = "TO_DATE";
    public static final String LEVEL = "level";
    public static final String NATION = "nation";
    public static final String POSITION = "position";
    public static final String DEPARTMENT = "department";
    public static final String GENDER = "gender";
    public static final String FULL_NAME = "fullName";
    public static final String UNIQUE_NUMBER = "uniqueNumber";
    public static final String TYPE = "TYPE";
    public static final String NAME = "name";
    public static final String SERVICE_NAME = "serviceName";
    public static final String METHOD_NAME = "methodName";
    public static final String PARENT_ID = "parentId";
    private static final String CATEGORY = "CATEGORY";
    private static final String CATEGORY_ID = "CATEGORY_ID";
    private static final String CATEGORY_ITEM = "CATEGORY_ITEM";
    private static final String COMPANY_ID = "COMPANY_ID";
    private static final String PARENT_COMPANY_ID = "PARENT_COMPANY_ID";
    private static final String EMPLOYEE_TYPE = "EMPLOYEE_TYPE";
    private static final String MODULE = "MODULE";
    private int start = 0;
    private int size = 10;
    private String searchKey = "";
    private HashMap<String, String> valueMap = null;
    private HashMap<String, List<String>> valueMap2 = null;
    private String sortColumn;
    private Boolean sortType = true;
    private Long id;
    private List<Long> companies;

    public FilterParameters() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        if (start > 0)
            this.start = start;
        else
            this.start = 0;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getName() {
        return getString(NAME);
    }

    public void setName(String name) {
        addString(NAME, name);
    }

    public String getServiceName() {
        return getString(SERVICE_NAME);
    }

    public void setServiceName(String name) {
        addString(SERVICE_NAME, name);
    }

    public String getMethodName() {
        return getString(METHOD_NAME);
    }

    public void setMethodName(String name) {
        addString(METHOD_NAME, name);
    }

    public String getSearchKey() {
        return searchKey;
    }

    public void setSearchKey(String searchKey) {
        this.searchKey = searchKey;
    }

    public String getSearchQuery() {
        if (isValidSearchKey()) {
            return (" " + getSearchKey().trim() + " ").replaceAll("'", "''").replaceAll(" ", "%").toLowerCase();
        } else {
            return null;
        }
    }

    public boolean isValidSearchKey() {  //o'g'irlangan code ))
        if (this.getSearchKey() == null /*|| "".equals(this.getSearchKey())*/) {
            return false;
        }
        if (this.getSearchKey().trim().length() > 20) {
            setSearchKey(getSearchKey().substring(0, 20));
        }
        if (this.getSearchKey().trim().split(" ").length > 3) {
            String[] searchKeys = this.getSearchKey().trim().split(" ");
            this.setSearchKey("");
            String delimitr = "";
            for (String miniSearchKey : searchKeys) {
                this.setSearchKey(getSearchKey() + delimitr + miniSearchKey);
                delimitr = " ";
            }
        }
        return (
                this.getSearchKey() != null
                        //&& !this.getSearchKey().trim().equals("")
                        && this.getSearchKey().trim().split(" ").length < 6 && this.getSearchKey().trim().length() <= 20
        );
    }

    public Long getCompanyId() {
        return getLong(COMPANY_ID);
    }

    public void setCompanyId(Long companyId) {
        addLong(COMPANY_ID, companyId);
    }

    public Long getParentCompanyId() {
        return getLong(PARENT_COMPANY_ID);
    }

    public void setParentCompanyId(Long id) {
        addLong(PARENT_COMPANY_ID, id);
    }

    public String getEmployeeType() {
        return getString(EMPLOYEE_TYPE);
    }

    public void setEmployeeType(String employeeType) {
        addString(EMPLOYEE_TYPE, employeeType);
    }

    public String getModule() {
        return getString(MODULE);
    }

    public void setModule(String module) {
        addString(MODULE, module);
    }

    public String getCategory() {
        return getString(CATEGORY);
    }

    public void setCategory(String category) {
        addString(CATEGORY, category);
    }

    public Long getCategoryId() {
        return getLong(CATEGORY_ID);
    }

    public void setCategoryID(Long category) {
        addLong(CATEGORY_ID, category);
    }

    public String getCategoryItem() {
        return getString(CATEGORY_ITEM);
    }

    public void setCategoryItem(String categoryItem) {
        addString(CATEGORY_ITEM, categoryItem);
    }

    @JsonIgnore
    public HashMap<String, String> getInstance() {
        return valueMap = valueMap == null ? new HashMap<String, String>() : valueMap;
    }

    @JsonIgnore
    public HashMap<String, List<String>> getInstance2() {
        return valueMap2 = valueMap2 == null ? new HashMap<String, List<String>>() : valueMap2;
    }

    public Long getUserId() {
        return getLong(USER_ID);
    }

    public void setUserId(Long userId) {
        addLong(USER_ID, userId);
    }

    public Long getStudentId() {
        return getLong(STUDENT_ID);
    }

    public void setStudentId(Long studentId) {
        addLong(STUDENT_ID, studentId);
    }

    public Date getFromDate() {
        return getDate(FROM_DATE);
    }

    public void setFromDate(Date fromDate) {
        addDate(FROM_DATE, fromDate);
    }

    public Date getToDate() {
        return getDate(TO_DATE);
    }

    public void setToDate(Date toDate) {
        addDate(TO_DATE, toDate);
    }

    public Date getStartDate() {
        return getDate(DATE);
    }

    public void setStartDate(Date date) {
        addDate(DATE, date);
    }

    public Long getGender() {
        return getLong(GENDER);
    }

    public void setGender(Long gender) {
        addLong(GENDER, gender);
    }

    public Long getDepartment() {
        return getLong(DEPARTMENT);
    }

    public void setDepartment(Long department) {
        addLong(DEPARTMENT, department);
    }

    public Long getPosition() {
        return getLong(POSITION);
    }

    public void setPosition(Long position) {
        addLong(POSITION, position);
    }

    public Long getNation() {
        return getLong(NATION);
    }

    public void setNation(Long nation) {
        addLong(NATION, nation);
    }

    public Long getLevel() {
        return getLong(LEVEL);
    }

    public void setLevel(Long level) {
        addLong(LEVEL, level);
    }

    public String getSortColumn() {
        return sortColumn;
    }

    public void setSortColumn(String sortColumn) {
        this.sortColumn = sortColumn;
    }

    public Boolean getSortType() {
        return Boolean.TRUE.equals(sortType);
    }

    public void setSortType(Boolean sortType) {
        this.sortType = sortType;
    }

    public String getSortQuery() {
        if (getSortColumn() == null || getSortColumn().trim().length() < 1) return "";
        return " ORDER BY " + getSortColumn() + (getSortType() ? " ASC" : " DESC");
    }

    public String getFullName() {
        return getString(FULL_NAME);
    }

    public void setFullName(String fullName) {
        addString(FULL_NAME, fullName);
    }

    public void setNumber(String uniqueNumber) {
        addString(UNIQUE_NUMBER, uniqueNumber);
    }

    public String getUniqueNumber() {
        return getString(UNIQUE_NUMBER);
    }

    public Long getStatus() {
        return getLong(STATUS);
    }

    public void setStatus(Long status) {
        addLong(STATUS, status);
    }

    public String getType() {
        return getString(TYPE);
    }

    public void setType(String type) {
        addString(TYPE, type);
    }

    public Long getParentId() {
        return getLong(PARENT_ID);
    }

    public void setParentId(Long parentId) {
        addLong(PARENT_ID, parentId);
    }

    public List<Long> getCompanies() {
        return companies;
    }

    public void setCompanies(List<Long> companies) {
        this.companies = companies;
    }

    public FilterParameters add(String key, String value) {
        addString(key, value);
        return this;
    }
}