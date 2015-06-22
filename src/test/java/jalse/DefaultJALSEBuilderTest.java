package jalse;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

public class DefaultJALSEBuilderTest {

    JALSE jalse = null;

    @After
    public void after() {
	jalse = null;
    }

    @Test(expected = IllegalStateException.class)
    public void buildTest1() {
	final DefaultJALSE.Builder builder = DefaultJALSE.builder();
	builder.setForkJoinEngine();
	jalse = builder.build();
    }

    @Test(expected = IllegalStateException.class)
    public void buildTest2() {
	final DefaultJALSE.Builder builder = DefaultJALSE.builder();
	builder.setThreadPoolEngine();
	jalse = builder.build();
    }

    @Test(expected = IllegalStateException.class)
    public void buildTest3() {
	final DefaultJALSE.Builder builder = DefaultJALSE.builder().setRandomID().setNoEntityLimit()
		.setParallelismToProcessors();
	jalse = builder.build();
    }

    @Test(expected = IllegalStateException.class)
    public void buildTest4() {
	final DefaultJALSE.Builder builder = DefaultJALSE.builder();
	builder.setForkJoinEngine().setRandomID().setParallelism(2);
	jalse = builder.build();
    }

    @Test(expected = IllegalStateException.class)
    public void buildTest5() {
	final DefaultJALSE.Builder builder = DefaultJALSE.builder();
	builder.setForkJoinEngine().setParallelismToProcessors();
	jalse = builder.build();
    }

    @Test(expected = IllegalStateException.class)
    public void buildTest6() {
	final DefaultJALSE.Builder builder = DefaultJALSE.builder();
	builder.setThreadPoolEngine().setRandomID().setNoEntityLimit();
	jalse = builder.build();
    }

    @Test(expected = IllegalStateException.class)
    public void buildTest7() {
	final DefaultJALSE.Builder builder = DefaultJALSE.builder();
	builder.setForkJoinEngine().setRandomID().setNoEntityLimit();
	jalse = builder.build();
    }

    @Test
    public void buildTest8() {
	final DefaultJALSE.Builder builder = DefaultJALSE.builder().setRandomID().setNoEntityLimit()
		.setParallelismToProcessors().setForkJoinEngine();
	jalse = builder.build();
	Assert.assertNotNull(jalse);
    }
}
