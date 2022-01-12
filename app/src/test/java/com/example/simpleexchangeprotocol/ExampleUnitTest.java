package com.example.simpleexchangeprotocol;

import org.junit.Test;

import static org.junit.Assert.*;

import java.util.Vector;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void testGetPhotoPositionAsVector() {

        final int[][] givenPosition = new int[6][2];

        givenPosition[0][0] = 200;
        givenPosition[0][1] = 1100;
        givenPosition[1][0] = 1200;
        givenPosition[1][1] = 1100;
        givenPosition[2][0] = 200;
        givenPosition[2][1] = 1750;
        givenPosition[3][0] = 1200;
        givenPosition[3][1] = 1750;
        givenPosition[4][0] = 200;
        givenPosition[4][1] = 2400;
        givenPosition[5][0] = 1200;
        givenPosition[5][1] = 2400;


        Vector<Integer> calculatedPosition = NewContract.getPhotoPosition(2);

        assertEquals(200, (int) calculatedPosition.get(0));
        assertEquals(1750, (int) calculatedPosition.get(1));
    }
}