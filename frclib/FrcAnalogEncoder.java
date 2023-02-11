/*
 * Copyright (c) 2023 Titan Robotics Club (http://www.titanrobotics.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package TrcFrcLib.frclib;

import TrcCommonLib.trclib.TrcAnalogInput;
import TrcCommonLib.trclib.TrcCardinalConverter;
import TrcCommonLib.trclib.TrcAnalogInput.DataType;

/**
 * This class implements an Analog Absolute Encoders that implements the FrcEncoder interface to allow compatibility
 * to other types of encoders.
 */
public class FrcAnalogEncoder implements FrcEncoder
{
    private final String instanceName;
    private final FrcAnalogInput analogInput;
    private final TrcCardinalConverter<TrcAnalogInput.DataType> cardinalConverter;
    private double sign = 1.0;
    private double scale = 1.0;
    private double offset = 0.0;

    /**
     * Constructor: Creates an instance of the object.
     *
     * @param instanceName specifies the instance name.
     * @param channel specifies the analog channel for the encoder.
     * @param powerRailIs3V3 specifies true if analog power rail is 3.3V, false if 5V.
     */
    public FrcAnalogEncoder(String instanceName, int channel, boolean powerRailIs3V3)
    {
        this.instanceName = instanceName;
        analogInput = new FrcAnalogInput(instanceName, channel, null, powerRailIs3V3);
        cardinalConverter = new TrcCardinalConverter<TrcAnalogInput.DataType>(
            instanceName, analogInput, TrcAnalogInput.DataType.NORMALIZED_DATA);
        // Data provided to the Cardinal Converter is normalized to the range of 0.0 to 1.0 regardless of the analog
        // power rail being 3.3V or 5V.
        cardinalConverter.setCardinalRange(0, 0.0, 1.0);
    }   //FrcAnalogEncoder

    /**
     * Constructor: Creates an instance of the object.
     *
     * @param instanceName specifies the instance name.
     * @param channel specifies the analog channel for the encoder.
     */
    public FrcAnalogEncoder(String instanceName, int channel)
    {
        this(instanceName, channel, false);
    }   //FrcAnalogEncoder

    /**
     * This method returns the instance name.
     *
     * @return instance name.
     */
    @Override
    public String toString()
    {
        return instanceName;
    }   //toString

    /**
     * This method enables/disables the Cardinal Converter task.
     *
     * @param enabled specifies true to enable cardinal converter, false to disable.
     */
    public void setEnabled(boolean enabled)
    {
        cardinalConverter.setEnabled(enabled);
    }   //setEnabled

    /**
     * This method checks if the Cardinal Converter task is enabled.
     *
     * @return true if cardinal converter is enabled, false if disabled.
     */
    public boolean isEnabled()
    {
        return cardinalConverter.isEnabled();
    }   //isEnabled

    //
    // Implements the FrcEncoder interface.
    //

    /**
     * This method resets the encoder revolution counter (Cardinal Converter).
     */
    @Override
    public void reset()
    {
        cardinalConverter.reset(0);
    }   //reset

    /**
     * This method reads the raw analog input of the encoder.
     *
     * @return raw input of the encoder in the unit of percent rotation (0 to 1).
     */
    @Override
    public double getRawPosition()
    {
        return analogInput.getRawData(0, DataType.RAW_DATA).value;
    }   //getRawPosition

    /**
     * This method returns the encoder position adjusted by scale and offset.
     *
     * @return encoder position adjusted by scale and offset.
     */
    @Override
    public double getPosition()
    {
        return sign * (cardinalConverter.getCartesianData(0).value - offset) * scale;
    }   //getPosition

    /**
     * This method reverses the direction of the encoder.
     *
     * @param inverted specifies true to reverse the encoder direction, false otherwise.
     */
    @Override
    public void setInverted(boolean inverted)
    {
        sign = inverted ? -1.0 : 1.0;
    }   //setInverted

    /**
     * This method sets the encoder scale and offset.
     *
     * @param scale specifies the scale value.
     * @param offset specifies the offset value.
     */
    @Override
    public void setScaleAndOffset(double scale, double offset)
    {
        this.scale = scale;
        this.offset = offset;
    }   //setScaleAndOffset

}   //class FrcAnalogEncoder
