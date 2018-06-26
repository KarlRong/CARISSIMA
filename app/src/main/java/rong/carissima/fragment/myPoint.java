package rong.carissima.fragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

import com.cocoahero.android.geojson.GeoJSON;
import com.cocoahero.android.geojson.Geometry;
import com.cocoahero.android.geojson.Position;

public class myPoint extends Geometry {

    // ------------------------------------------------------------------------
    // Instance Variables
    // ------------------------------------------------------------------------

    private myPosition mPosition;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    public myPoint() {
        // Default Constructor
    }

    public myPoint(JSONObject json) {
        super(json);

        this.setPosition(json.optJSONArray(JSON_COORDINATES));
    }

    public myPoint(JSONArray position) {
        this.setPosition(position);
    }

    public myPoint(myPosition position) {
        this.setPosition(position);
    }

    public myPoint(double latitude, double longitude) {
        this.mPosition = new myPosition(latitude, longitude);
    }

    public myPoint(double latitude, double longitude, double altitude) {
        this.mPosition = new myPosition(latitude, longitude, altitude);
    }

    // ------------------------------------------------------------------------
    // Parcelable Interface
    // ------------------------------------------------------------------------

    public static final Parcelable.Creator<com.cocoahero.android.geojson.Point> CREATOR = new Creator<com.cocoahero.android.geojson.Point>() {
        @Override
        public com.cocoahero.android.geojson.Point createFromParcel(Parcel in) {
            return (com.cocoahero.android.geojson.Point) readParcel(in);
        }

        @Override
        public com.cocoahero.android.geojson.Point[] newArray(int size) {
            return new com.cocoahero.android.geojson.Point[size];
        }
    };

    // ------------------------------------------------------------------------
    // Public Methods
    // ------------------------------------------------------------------------

    public myPosition getPosition() {
        return this.mPosition;
    }

    public void setPosition(myPosition position) {
        this.mPosition = position;
    }

    public void setPosition(JSONArray position) {
        if (position != null) {
            this.mPosition = new myPosition(position);
        }
        else {
            this.mPosition = null;
        }
    }

    @Override
    public String getType() {
        return GeoJSON.TYPE_POINT;
    }

    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject json = super.toJSON();

        if (this.mPosition != null) {
            json.put(JSON_COORDINATES, this.mPosition.toJSON());
        }

        return json;
    }

}
