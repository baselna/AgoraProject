package com.example.myapplication;

public class minimal_product {
    private String name;
    private String city;
    private String cond;
    private int id;

    public minimal_product(String name, String city, int numCond, int id) {
        this.name = name;
        this.city = city;
        //int converted_cond = Integer.parseInt(cond);
        switch (numCond){
            case 1:
               this.cond = "very poor";
               break;
            case 2:
                this.cond = "poor";
                break;
            case 3:
                this.cond = "fair";
                break;
            case 4:
                this.cond = "good";
                break;
            case 5:
                this.cond = "very good";
        }
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCond() {
        return cond;
    }

    public void setCond(String cond) {
        this.cond = cond;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}

