package jalse;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

public class JALSEBuilderTest {

    JALSE jalse = null;

    @After
    public void after() {
	jalse = null;
    }

    @Test
    public void buildCommonPoolJALSETest() {
	jalse = JALSEBuilder.buildCommonPoolJALSE();
	Assert.assertNotNull(jalse);
    }

    @Test
    public void buildManualJALSETest() {
	jalse = JALSEBuilder.buildManualJALSE();
	Assert.assertNotNull(jalse);
    }

    @Test
    public void buildSingleThreadedJALSETest() {
	jalse = JALSEBuilder.buildSingleThreadedJALSE();
	Assert.assertNotNull(jalse);
    }

    @Test(expected = IllegalStateException.class)
    public void buildTest1() {
	final JALSEBuilder builder = JALSEBuilder.newBuilder();
	builder.setForkJoinEngine();
	jalse = builder.build();
    }

    @Test(expected = IllegalStateException.class)
    public void buildTest2() {
	final JALSEBuilder builder = JALSEBuilder.newBuilder();
	builder.setThreadPoolEngine();
	jalse = builder.build();
    }

    @Test(expected = IllegalStateException.class)
    public void buildTest3() {
	final JALSEBuilder builder = JALSEBuilder.newBuilder().setRandomID().setNoEntityLimit()
		.setParallelismToProcessors();
	jalse = builder.build();
    }

    @Test(expected = IllegalStateException.class)
    public void buildTest4() {
	final JALSEBuilder builder = JALSEBuilder.newBuilder();
	builder.setForkJoinEngine().setRandomID().setParallelism(2);
	jalse = builder.build();
    }

    @Test(expected = IllegalStateException.class)
    public void buildTest5() {
	final JALSEBuilder builder = JALSEBuilder.newBuilder();
	builder.setForkJoinEngine().setParallelismToProcessors();
	jalse = builder.build();
    }

    @Test(expected = IllegalStateException.class)
    public void buildTest6() {
	final JALSEBuilder builder = JALSEBuilder.newBuilder();
	builder.setThreadPoolEngine().setRandomID().setNoEntityLimit();
	jalse = builder.build();
    }

    @Test(expected = IllegalStateException.class)
    public void buildTest7() {
	final JALSEBuilder builder = JALSEBuilder.newBuilder();
	builder.setForkJoinEngine().setRandomID().setNoEntityLimit();
	jalse = builder.build();
    }

    @Test
    public void buildTest8() {
	final JALSEBuilder builder = JALSEBuilder.newBuilder().setRandomID().setNoEntityLimit()
		.setParallelismToProcessors().setForkJoinEngine();
	jalse = builder.build();
	Assert.assertNotNull(jalse);
    }
}
