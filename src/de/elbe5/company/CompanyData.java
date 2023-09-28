/*
 Bandika CMS - A Java based modular Content Management System
 Copyright (C) 2009-2021 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.company;

import de.elbe5.base.BaseData;
import de.elbe5.base.JsonObject;
import de.elbe5.request.RequestData;
import de.elbe5.request.RequestType;
import org.json.simple.JSONObject;

import java.time.LocalDate;

public class CompanyData extends BaseData {

    protected String name="";
    protected String street="";
    protected String zipCode="";
    protected String city="";
    protected String email="";
    protected String phone="";
    protected String notes="";

    // base data

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    // multiple data

    public void setNewId(){
        setId(CompanyBean.getInstance().getNextId());
    }

    @Override
    public void readRequestData(RequestData rdata, RequestType type) {
        switch (type){
            case api -> {
                super.readRequestData(rdata, type);
                setName(rdata.getAttributes().getString("name"));
                setStreet(rdata.getAttributes().getString("street"));
                setZipCode(rdata.getAttributes().getString("zipCode"));
                setCity(rdata.getAttributes().getString("city"));
                setEmail(rdata.getAttributes().getString("email"));
                setPhone(rdata.getAttributes().getString("phone"));
                setNotes(rdata.getAttributes().getString("notes"));
            }
            case backend -> {
                setName(rdata.getAttributes().getString("name"));
                setStreet(rdata.getAttributes().getString("street"));
                setZipCode(rdata.getAttributes().getString("zipCode"));
                setCity(rdata.getAttributes().getString("city"));
                setEmail(rdata.getAttributes().getString("email"));
                setPhone(rdata.getAttributes().getString("phone"));
                setNotes(rdata.getAttributes().getString("notes"));
            }
        }
    }

    @Override
    public JsonObject getJson(){
        return super.getJson()
                .add("name", getName())
                .add("street", street)
                .add("zipCode", zipCode)
                .add("city", city)
                .add("email", email)
                .add("phone", phone)
                .add("notes", notes);
    }

    @Override
    public void fromJson(JSONObject json) {
        super.fromJson(json);
        setName(getString(json, "name"));
        setStreet(getString(json, "street"));
        setZipCode(getString(json, "zipCode"));
        setCity(getString(json, "city"));
        setEmail(getString(json, "email"));
        setPhone(getString(json, "phone"));
        setNotes(getString(json, "notes"));
    }

}
