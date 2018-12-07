package com.uzmap.pkg.uzmodules.uzBMap;

import com.uzmap.pkg.uzcore.uzmodule.UZModule;
import com.uzmap.pkg.uzcore.uzmodule.UZModuleContext;

import android.R.integer;

public class BMapConfig {
	
	private static BMapConfig instance;
	
	private BMapConfig() {
		
	}
	
	public static BMapConfig getInstance() {
		if (instance == null) {
			instance = new BMapConfig();
		}
		return instance;
	}
	
	private UZModuleContext addWebBubbleModuleContext;
	public void setAddWebBubble(UZModuleContext moduleContext) {
		this.addWebBubbleModuleContext = moduleContext;
	}
	
	public UZModuleContext getAddWebBubble() {
		return addWebBubbleModuleContext;
	}
	

}
