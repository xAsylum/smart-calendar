package com.example.smartcalendar.data.models.photon;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PhotonResponse {
    @SerializedName("features")
    private List<Feature> features;

    public List<Feature> getFeatures() { return features; }

    public static class Feature {
        @SerializedName("geometry")
        private Geometry geometry;
        @SerializedName("properties")
        private Properties properties;

        public Geometry getGeometry() { return geometry; }
        public Properties getProperties() { return properties; }
    }

    public static class Geometry {
        @SerializedName("coordinates")
        private List<Double> coordinates;

        public List<Double> getCoordinates() { return coordinates; }
    }

    public static class Properties {
        @SerializedName("name")
        private String name;
        @SerializedName("city")
        private String city;
        @SerializedName("street")
        private String street;
        @SerializedName("housenumber")
        private String housenumber;
        @SerializedName("country")
        private String country;

        public String getDisplayName() {
            StringBuilder sb = new StringBuilder();
            if (name != null) sb.append(name);
            if (street != null) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(street);
            }
            if (housenumber != null) sb.append(" ").append(housenumber);
            if (city != null) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(city);
            }
            if (country != null) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(country);
            }
            return sb.toString();
        }
    }
}
