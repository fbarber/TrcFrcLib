package frclib;

import com.playingwithfusion.TimeOfFlight;
import TrcCommonLib.trclib.TrcDistanceSensor;

public class FrcCANTimeOfFlight extends TimeOfFlight implements TrcDistanceSensor
{
    /**
     * Create an instance of the CAN Time Of Flight sensor.
     * <p>
     * This is designed to support the Playing With Fusion (PWF) SEN-36005 time of
     * flight sensor
     *
     * @param instanceName The instance name of this sensor.
     * @param sensorId     The 6-bit identifier used to select a particular
     *                     sensor on the CAN bus.  This identifier may be set
     *                     through the PWF Device configuration page on the
     *                     roboRIO.
     */
    public FrcCANTimeOfFlight(String instanceName, int sensorId)
    {
        super(sensorId);
    }

    @Override
    public double getDistanceInches()
    {
        double distMeters = getRange() / 1000.0;
        return distMeters / TrcDistanceSensor.METERS_PER_INCH;
    }
}
