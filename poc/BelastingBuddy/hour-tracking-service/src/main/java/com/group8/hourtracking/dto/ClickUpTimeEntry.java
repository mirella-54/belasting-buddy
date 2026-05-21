package com.group8.hourtracking.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * DTO for ClickUp time entry response (from their api)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClickUpTimeEntry {
  /** id */
    private String id;
    /** worspace id */
    private String wid; // workspace id
    /** task */
    private ClickUpTask task;
    /** clickup user */
    private ClickUpUser user;
    /** billable */
    private Boolean billable;
    /** start */
    private String start; // Unix time in milliseconds
    /** end */
    private String end; // Unix time in milliseconds
    /** duration */
    private String duration; // Duration in milliseconds
    /** description */
    private String description;
    /** tags */
    private List<ClickUpTag> tags;
    /** source */
    private String source;
    /** at */
    private String at; // Unix time in milliseconds
    /** is locked */
    private Boolean isLocked;
    /** approval id. */
    private String approvalId;
    /** task location */
    private ClickUpTaskLocation taskLocation;
    /** task url */
    private String taskUrl;

    /**
     * get id
     * @return id
     */
    public String getId() {
        return id;
    }
    /**
     * set id
     * @param id
     */
    public void setId(String id) {
        this.id = id;
    }
    /**
     * get wid
     * @return wid
     */
    public String getWid() {
        return wid;
    }
    /**
     * set wid
     * @param wid
     */
    public void setWid(String wid) {
        this.wid = wid;
    }
    /**
     * get task
     * @return task
     */
    public ClickUpTask getTask() {
        return task;
    }
    /**
     * set task
     * @param task
     */
    public void setTask(ClickUpTask task) {
        this.task = task;
    }
    /**
     * get user
     * @return user
     */
    public ClickUpUser getUser() {
        return user;
    }
    /**
     * set user
     * @param user
     */
    public void setUser(ClickUpUser user) {
        this.user = user;
    }
    /**
     * get billable
     * @return billable
     */
    public Boolean getBillable() {
        return billable;
    }
    /**
     * set billable
     * @param billable
     */
    public void setBillable(Boolean billable) {
        this.billable = billable;
    }
    /**
     * get start
     * @return start
     */
    public String getStart() {
        return start;
    }
    /**
     * set start
     * @param start
     */
    public void setStart(String start) {
        this.start = start;
    }
    /**
     * get end
     * @return end
     */
    public String getEnd() {
        return end;
    }
    /**
     * set end
     * @param end
     */
    public void setEnd(String end) {
        this.end = end;
    }
    /**
     * get duration
     * @return duration
     */
    public String getDuration() {
        return duration;
    }
    /**
     * set duration
     * @param duration
     */
    public void setDuration(String duration) {
        this.duration = duration;
    }
    /**
     * get description
     * @return description
     */
    public String getDescription() {
        return description;
    }
    /**
     * set description
     * @param description
     */
    public void setDescription(String description) {
        this.description = description;
    }
    /**
     * get tags
     * @return tags
     */
    public List<ClickUpTag> getTags() {
        return tags;
    }
/**
     * set tags
     * @param tags
     */
    public void setTags(List<ClickUpTag> tags) {
        this.tags = tags;
    }

    /**
     * get source
     * @return source
     */
    public String getSource() {
        return source;
    }

    /**
     * set source
     * @param source
     */
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * get at
     * @return at
     */
    public String getAt() {
        return at;
    }

    /**
     * set at
     * @param at
     */
    public void setAt(String at) {
        this.at = at;
    }

    /**
     * get isLocked
     * @return isLocked
     */
    public Boolean getIsLocked() {
        return isLocked;
    }

    /**
     * set isLocked
     * @param isLocked
     */
    public void setIsLocked(Boolean isLocked) {
        this.isLocked = isLocked;
    }

    /**
     * get approvalId
     * @return approvalId
     */
    public String getApprovalId() {
        return approvalId;
    }

    /**
     * set approvalId
     * @param approvalId
     */
    public void setApprovalId(String approvalId) {
        this.approvalId = approvalId;
    }

    /**
     * get taskLocation
     * @return taskLocation
     */
    public ClickUpTaskLocation getTaskLocation() {
        return taskLocation;
    }

    /**
     * set taskLocation
     * @param taskLocation
     */
    public void setTaskLocation(ClickUpTaskLocation taskLocation) {
        this.taskLocation = taskLocation;
    }

    /**
     * get taskUrl
     * @return taskUrl
     */
    public String getTaskUrl() {
        return taskUrl;
    }

    /**
     * set taskUrl
     * @param taskUrl
     */
    public void setTaskUrl(String taskUrl) {
        this.taskUrl = taskUrl;
    }

    /**
     * Nested class for Task info
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ClickUpTask {
        /** id */
        private String id;
        /** name*/
        private String name;
        /** status */
        private ClickUpStatus status;
        /** custom type */
        private String customType;

        /**
         * get id
         * @return id
         */
        public String getId() {
            return id;
        }

        /**
         * set id
         * @param id
         */
        public void setId(String id) {
            this.id = id;
        }

        /**
         * get name
         * @return name
         */
        public String getName() {
            return name;
        }

        /**
         * set name
         * @param name
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * get status
         * @return status
         */
        public ClickUpStatus getStatus() {
            return status;
        }

        /**
         * set status
         * @param status
         */
        public void setStatus(ClickUpStatus status) {
            this.status = status;
        }

        /**
         * get customType
         * @return customType
         */
        public String getCustomType() {
            return customType;
        }

        /**
         * set customType
         * @param customType
         */
        public void setCustomType(String customType) {
            this.customType = customType;
        }
    }

    /**
     * Nested class for Status info
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ClickUpStatus {
        /** status. */
        private String status;
        /** color */
        private String color;
        /** type */
        private String type;
        /** order index */
        private Integer orderindex;

        /**
         * get status
         * @return status
         */
        public String getStatus() {
            return status;
        }

        /**
         * set status
         * @param status
         */
        public void setStatus(String status) {
            this.status = status;
        }

        /**
         * get color
         * @return color
         */
        public String getColor() {
            return color;
        }

        /**
         * set color
         * @param color
         */
        public void setColor(String color) {
            this.color = color;
        }

        /**
         * get type
         * @return type
         */
        public String getType() {
            return type;
        }

        /**
         * set type
         * @param type
         */
        public void setType(String type) {
            this.type = type;
        }

        /**
         * get orderindex
         * @return orderindex
         */
        public Integer getOrderindex() {
            return orderindex;
        }

        /**
         * set orderindex
         * @param orderindex
         */
        public void setOrderindex(Integer orderindex) {
            this.orderindex = orderindex;
        }
    }

    /**
     * Nested class for Task Location
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ClickUpTaskLocation {
        /** list id. */
        private String listId;
        /** folder id. */
        private String folderId;
        /** space id. */
        private String spaceId;

        /**
         * get listId
         * @return listId
         */
        public String getListId() {
            return listId;
        }

        /**
         * set listId
         * @param listId
         */
        public void setListId(String listId) {
            this.listId = listId;
        }

        /**
         * get folderId
         * @return folderId
         */
        public String getFolderId() {
            return folderId;
        }

        /**
         * set folderId
         * @param folderId
         */
        public void setFolderId(String folderId) {
            this.folderId = folderId;
        }

        /**
         * get spaceId
         * @return spaceId
         */
        public String getSpaceId() {
            return spaceId;
        }

        /**
         * set spaceId
         * @param spaceId
         */
        public void setSpaceId(String spaceId) {
            this.spaceId = spaceId;
        }
    }

    /**
     * Nested class for Tags
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ClickUpTag {
        /** name. */
        private String name;
        /** tag fg */
        private String tagFg;
        /** tag bg */
        private String tagBg;
        /** creator */
        private String creator;

        /**
         * get name
         * @return name
         */
        public String getName() {
            return name;
        }

        /**
         * set name
         * @param name
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * get tagFg
         * @return tagFg
         */
        public String getTagFg() {
            return tagFg;
        }

        /**
         * set tagFg
         * @param tagFg
         */
        public void setTagFg(String tagFg) {
            this.tagFg = tagFg;
        }

        /**
         * get tagBg
         * @return tagBg
         */
        public String getTagBg() {
            return tagBg;
        }

        /**
         * set tagBg
         * @param tagBg
         */
        public void setTagBg(String tagBg) {
            this.tagBg = tagBg;
        }

        /**
         * get creator
         * @return creator
         */
        public String getCreator() {
            return creator;
        }

        /**
         * set creator
         * @param creator
         */
        public void setCreator(String creator) {
            this.creator = creator;
        }
    }
}
