/*
 * Copyright (c) 2019 Titan Robotics Club (http://www.titanrobotics.com)
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

import com.google.gson.Gson;
import edu.wpi.first.networktables.NetworkTableEntry;
import TrcCommonLib.trclib.TrcUtil;

public class FrcRaspiVisionProcessor extends FrcRemoteVisionProcessor
{
    private Gson gson;
    private NetworkTableEntry data;

    public FrcRaspiVisionProcessor(String instanceName, String networkTable, String dataKey, int relayPort)
    {
        super(instanceName, networkTable, relayPort);
        data = super.networkTable.getEntry(dataKey);
        gson = new Gson();
    }

    @Override protected RelativePose processData()
    {
        String dataString = data.getString("");
        if ("".equals(dataString))
        {
            return null;
        }
        RelativePose pose = gson.fromJson(dataString, RelativePose.class);
        pose.time = TrcUtil.getCurrentTime();
        return pose;
    }
}
