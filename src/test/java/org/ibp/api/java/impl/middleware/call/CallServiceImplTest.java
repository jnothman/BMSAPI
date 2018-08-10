package org.ibp.api.java.impl.middleware.call;

import org.ibp.api.java.calls.CallService;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;

public class CallServiceImplTest {

	private CallService callService = new CallServiceImpl();

	@Test
	public void testGetAllCalls() {

		final List<Map<String, Object>> result = callService.getAllCalls(null, 10, 0);
		Assert.assertEquals("First page should contain 10 records",10, result.size());

		final List<Map<String, Object>> result2 = callService.getAllCalls(null, 10, 1);
		Assert.assertEquals("Second page should contain 2 records", 2, result2.size());

		final List<Map<String, Object>> result3 = callService.getAllCalls(null, null, null);
		Assert.assertEquals("Should return all records if pageSize and pageNumber are not specified",12, result3.size());

		final List<Map<String, Object>> result4 = callService.getAllCalls("csv", 10, 0);
		Assert.assertEquals("There is only one call service with csv datatype", 1, result4.size());

	}

}
