package com.uzmap.pkg.uzmodules.uzBMap;

import java.io.IOException;
import java.io.InputStream;

import com.uzmap.pkg.uzcore.UZCoreUtil;
import com.uzmap.pkg.uzcore.uzmodule.UZModuleContext;

import android.content.Context;

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
	
	public static void copyFile(Context context, int mode, String themeParent, String file) {
		String fileName = "";
		if (mode == 1) {
			fileName = "blackNight";
		}else if (mode == 2) {
			fileName = "freshBlue";
		}else if (mode == 3) {
			fileName = "midnightBlue";
		}
		InputStream inputStream = null;
		try {
			inputStream = context.getAssets().open("customConfigdir/" + fileName + "/custom_config");
			String data = UZCoreUtil.readString(inputStream);
			UZCoreUtil.writeString(file, data, false);
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (IOException e2) {
				e2.printStackTrace();
			}
			
		}
	}
	

}
