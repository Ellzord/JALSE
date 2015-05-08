package jalse;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class JALSEBuilderTest {
	
	JALSE jalse = null;
	
	@After
	public void after(){
		jalse = null;
	}
	
	@Test
	public void buildCommonPoolJALSETest(){
		jalse = JALSEBuilder.buildCommonPoolJALSE();
		Assert.assertNotNull(jalse);
	}
	
	@Test
	public void buildManualJALSETest(){
		jalse = JALSEBuilder.buildManualJALSE();
		Assert.assertNotNull(jalse);
	}
	
	@Test
	public void buildSingleThreadedJALSETest(){
		jalse = JALSEBuilder.buildSingleThreadedJALSE();
		Assert.assertNotNull(jalse);
	}
	
	@Test(expected=IllegalStateException.class)
	public void buildTest1(){
		JALSEBuilder builder = JALSEBuilder.newBuilder();
		builder.setForkJoinEngine();
		jalse = builder.build();
	}
	
	@Test(expected=IllegalStateException.class)
	public void buildTest2(){
		JALSEBuilder builder = JALSEBuilder.newBuilder();
		builder.setThreadPoolEngine();
		jalse = builder.build();
	}
	
	@Test(expected=IllegalStateException.class)
	public void buildTest3(){
		JALSEBuilder builder = JALSEBuilder.newBuilder().setDummyID().setNoEntityLimit().setParallelismToProcessors();
		jalse = builder.build();
	}
	
	@Test(expected=IllegalStateException.class)
	public void buildTest4(){
		JALSEBuilder builder = JALSEBuilder.newBuilder();
		builder.setForkJoinEngine().setDummyID().setParallelism(2);
		jalse = builder.build();
	}
	
	@Test(expected=IllegalStateException.class)
	public void buildTest5(){
		JALSEBuilder builder = JALSEBuilder.newBuilder();
		builder.setForkJoinEngine().setParallelismToProcessors();
		jalse = builder.build();
	}
	
	@Test(expected=IllegalStateException.class)
	public void buildTest6(){
		JALSEBuilder builder = JALSEBuilder.newBuilder();
		builder.setThreadPoolEngine().setDummyID().setNoEntityLimit();
		jalse = builder.build();
	}
	
	@Test(expected=IllegalStateException.class)
	public void buildTest7(){
		JALSEBuilder builder = JALSEBuilder.newBuilder();
		builder.setForkJoinEngine().setDummyID().setNoEntityLimit();
		jalse = builder.build();
	}
	
	@Test
	public void buildTest8(){
		JALSEBuilder builder = JALSEBuilder.newBuilder().setDummyID().setNoEntityLimit().setParallelismToProcessors().setForkJoinEngine();
		jalse = builder.build();
		Assert.assertNotNull(jalse);
	}
}
