package org.example.core.models.dto;

public class Project {
    private int id;
    private String name;
    private String description;
    private String image;
    private int reward;

    public Project() {
    }


    public Project(int id, String name, String description, String image, int reward) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.image = image;
        this.reward = reward;
    }

    // Геттеры и сеттеры
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getReward() {
        return reward;
    }

    public void setReward(int reward) {
        this.reward = reward;
    }
}

