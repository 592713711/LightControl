package com.zsg.jx.lightcontrol.bin;

/**
 * Created by zsg on 2016/8/16.
 */
public class User {
    private String id;
    private String name;
    private String phone;
    private String sex;
    private String password;
    private String create_time;
    private String image_thumb;
    private String image;
    private String token;

    public User(String id, String name, String phone, String sex,
                String password, String create_time, String image_thumb,
                String image, String token) {
        super();
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.sex = sex;
        this.password = password;
        this.create_time = create_time;
        this.image_thumb = image_thumb;
        this.image = image;
        this.token = token;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }
    public String getSex() {
        return sex;
    }
    public void setSex(String sex) {
        this.sex = sex;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public String getCreate_time() {
        return create_time;
    }
    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }
    public String getImage_thumb() {
        return image_thumb;
    }
    public void setImage_thumb(String image_thumb) {
        this.image_thumb = image_thumb;
    }
    public String getImage() {
        return image;
    }
    public void setImage(String image) {
        this.image = image;
    }
    public String getToken() { return token; }
    @Override
    public String toString() {
        return "User [id=" + id + ", name=" + name + ", phone=" + phone
                + ", sex=" + sex + ", password=" + password + ", create_time="
                + create_time + ", token = " + token + ", image_thumb=" + image_thumb + ", image="
                + image + "]";
    }

}
