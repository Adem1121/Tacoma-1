package ftdis.fdpu;

import org.junit.*;
import static org.junit.Assert.*;

/**
 * Unit test TurnSegment methods
 *
 * @author  windowSeatFSX@gmail.com
 * @version 0.1
 */
public class TurnSegmentTest {

    TurnSegment turnSgm01 = new TurnSegment(),
            turnSgm02 = new TurnSegment(),
            turnSgm03 = new TurnSegment();

    @Before
    public void setUp(){
        // 01 Wpt 2
        turnSgm01.setStartPt(47.480037, -122.307688);
        turnSgm01.setTurnPt(47.485759, -122.307647);
        turnSgm01.setEndPt(47.490131, -122.3021864);

        // 02 Wpt 5
        turnSgm02.setStartPt(47.538169, -122.403729);
        turnSgm02.setTurnPt(47.527899, -122.424960);
        turnSgm02.setEndPt(47.510266, -122.424718);

        // 03 Wpt 6
        turnSgm03.setStartPt(47.406050, -122.423292);
        turnSgm03.setTurnPt(47.362018, -122.422692);
        turnSgm03.setEndPt(47.373599, -122.359965);
    }

    @Test
    public void testGetCourseStart() throws Exception {
        assertEquals(0.28, turnSgm01.getCourseStart(), 0.01);
        assertEquals(234.38, turnSgm02.getCourseStart(), 0.01);
        assertEquals(179.47, turnSgm03.getCourseStart(), 0.01);
    }

    @Test
    public void testGetCourseEnd() throws Exception {
        assertEquals(40.16,turnSgm01.getCourseEnd(),0.01);
        assertEquals(179.47,turnSgm02.getCourseEnd(),0.01);
        assertEquals(74.78,turnSgm03.getCourseEnd(),0.01);
    }

    @Test
    public void testGetCourseChange() throws Exception {
        assertEquals(39.88,turnSgm01.getCourseChange(),0.01);
        assertEquals(-54.91,turnSgm02.getCourseChange(),0.01);
        assertEquals(-104.69,turnSgm03.getCourseChange(),0.01);
    }

    @Test
    public void testGetDist() throws Exception {
        Waypoint tp;

        // 01
        tp = new Waypoint(turnSgm01.getTurnPt());
        turnSgm01.alignToTrack(tp);
        assertEquals(1220.62, turnSgm01.getDist(turnSgm01.getStartPt(), turnSgm01.getEndPt()), 0.01);
        assertEquals(610.38, turnSgm01.getDist(turnSgm01.getStartPt(), tp), 0.01);
        assertEquals(-610.22,turnSgm01.getDist(turnSgm01.getEndPt(),tp), 0.01);

        // 02
        tp = new Waypoint(turnSgm02.getTurnPt());
        turnSgm02.alignToTrack(tp);
        assertEquals(3616.88, turnSgm02.getDist(turnSgm02.getStartPt(), turnSgm02.getEndPt()), 0.01);
        assertEquals(1808.03, turnSgm02.getDist(turnSgm02.getStartPt(), tp), 0.01);
        assertEquals(-1808.87,turnSgm02.getDist(turnSgm02.getEndPt(),tp), 0.01);

        // 03
        tp = new Waypoint(turnSgm03.getTurnPt());
        turnSgm03.alignToTrack(tp);
        assertEquals(6899.49, turnSgm03.getDist(turnSgm03.getStartPt(), turnSgm03.getEndPt()), 0.01);
        assertEquals(3450.13, turnSgm03.getDist(turnSgm03.getStartPt(), tp), 0.01);
        assertEquals(-3449.98,turnSgm03.getDist(turnSgm03.getEndPt(),tp), 0.01);
    }

    @Test
    public void testGetCourseAtWpt() throws Exception{
        assertEquals(20.22,turnSgm01.getCourseAtWpt(turnSgm01.getItmWpt(turnSgm01.getStartPt(),turnSgm01.getDist(turnSgm01.getStartPt(), turnSgm01.getEndPt())/2)),0.01);
        assertEquals(206.92,turnSgm02.getCourseAtWpt(turnSgm02.getItmWpt(turnSgm02.getStartPt(),turnSgm02.getDist(turnSgm02.getStartPt(), turnSgm02.getEndPt())/2)),0.01);
        assertEquals(127.13,turnSgm03.getCourseAtWpt(turnSgm03.getItmWpt(turnSgm03.getStartPt(),turnSgm03.getDist(turnSgm03.getStartPt(), turnSgm03.getEndPt())/2)),0.01);
    }

    @Test
    public void testGetTrackError() throws Exception{
        assertEquals(111.79, turnSgm01.getTrackError(turnSgm01.getTurnPt()), 0.01);
        assertEquals(479.88, turnSgm02.getTrackError(turnSgm02.getTurnPt()), 0.01);
        assertEquals(2408.27, turnSgm03.getTrackError(turnSgm03.getTurnPt()), 0.01);
    }


    @Ignore
    public void testAlignToTrack() throws Exception{
        // Implicitly tested in testGetDist()
    }

    @Ignore
    public void testGetItmWaypoint() throws Exception {
        // Implicitly tested in testGetCourseAtWpt()
    }
}