package org.freeplane.core.ui.menubuilders.generic;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;


public class EntryNavigatorTest {
	@Test
	public void findsChildByPath() throws Exception {
		final EntryNavigator entryNavigator = new EntryNavigator();
		Entry top = new Entry();
		Entry middle = new Entry();
		top.addChild(middle);
		middle.setName("middle");
		Entry down = new Entry();
		middle.addChild(down);
		down.setName("down");
		assertThat(entryNavigator.findChildByPath(top, "middle/down"), equalTo(down));
	}

	@Test
	public void findsChildByPathAlias() throws Exception {
		final EntryNavigator entryNavigator = new EntryNavigator();
		entryNavigator.addAlias("medium", "middle");
		Entry top = new Entry();
		Entry middle = new Entry();
		top.addChild(middle);
		middle.setName("middle");
		Entry down = new Entry();
		middle.addChild(down);
		down.setName("down");
		assertThat(entryNavigator.findChildByPath(top, "medium/down"), equalTo(down));
	}
}
