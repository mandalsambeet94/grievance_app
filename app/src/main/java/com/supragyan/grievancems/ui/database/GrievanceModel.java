package com.supragyan.grievancems.ui.database;

import org.json.JSONArray;

public class GrievanceModel {
    private String block;
    private String gp;
    private String village;
    private String address;
    private String wardNo;
    private String name;
    private String fatherName;
    private String contact;
    private String topic;
    private String grievanceMatter;
    private String remark;
    private String photos;
    private String grievanceID;
    private String uploadID;
    private String offlineID;
    private String userID;
    private JSONArray attachments;

    public JSONArray getAttachments() {
        return attachments;
    }

    public void setAttachments(JSONArray attachments) {
        this.attachments = attachments;
    }

    public String getOfflineID() {
        return offlineID;
    }

    public void setOfflineID(String offlineID) {
        this.offlineID = offlineID;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getBlock() {
        return block;
    }

    public void setBlock(String block) {
        this.block = block;
    }

    public String getGp() {
        return gp;
    }

    public void setGp(String gp) {
        this.gp = gp;
    }

    public String getVillage() {
        return village;
    }

    public void setVillage(String village) {
        this.village = village;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getWardNo() {
        return wardNo;
    }

    public void setWardNo(String wardNo) {
        this.wardNo = wardNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFatherName() {
        return fatherName;
    }

    public void setFatherName(String fatherName) {
        this.fatherName = fatherName;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getGrievanceMatter() {
        return grievanceMatter;
    }

    public void setGrievanceMatter(String grievanceMatter) {
        this.grievanceMatter = grievanceMatter;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getPhotos() {
        return photos;
    }

    public void setPhotos(String photos) {
        this.photos = photos;
    }

    public String getGrievanceID() {
        return grievanceID;
    }

    public void setGrievanceID(String grievanceID) {
        this.grievanceID = grievanceID;
    }

    public String getUploadID() {
        return uploadID;
    }

    public void setUploadID(String uploadID) {
        this.uploadID = uploadID;
    }
}
