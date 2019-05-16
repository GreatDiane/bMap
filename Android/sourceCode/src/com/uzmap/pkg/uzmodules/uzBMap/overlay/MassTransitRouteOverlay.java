package com.uzmap.pkg.uzmodules.uzBMap.overlay;

import java.util.ArrayList;
import java.util.List;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.search.route.MassTransitRouteLine;
import com.baidu.mapapi.search.route.MassTransitRouteLine.TransitStep.StepVehicleInfoType;

import android.os.Bundle;
import android.util.Log;

public class MassTransitRouteOverlay extends OverlayManager {
	private MassTransitRouteLine mRouteLine = null;

	/**
	 * 构造函数
	 * 
	 * @param baiduMap
	 *            该TransitRouteOverlay引用的 BaiduMap 对象
	 */
	public MassTransitRouteOverlay(BaiduMap baiduMap) {
		super(baiduMap);
	}

	@Override
	public final List<OverlayOptions> getOverlayOptions() {

		if (mRouteLine == null) {
			return null;
		}

		List<OverlayOptions> overlayOptionses = new ArrayList<OverlayOptions>();
        List<List<MassTransitRouteLine.TransitStep>> stepss = mRouteLine.getNewSteps();
		if (isSameCity()) {
			// 同城 (同城时，每个steps的get(i)对应的List是一条step的不同方案，此处都选第一条进行绘制，即get（0））
            // 我的理解：steps就是分几步达到终点，然后每一步有不同的方案，这里取每一步的第一种方案
            // step node
			if (stepss != null && stepss.size() > 0) {
				for (int i = 0; i < stepss.size(); i++) {
					MassTransitRouteLine.TransitStep step = stepss.get(i).get(0);
					Bundle b = new Bundle();
					b.putInt("index", i);
					if (step.getStartLocation() != null) {
						overlayOptionses.add((new MarkerOptions()).position(step.getStartLocation()).anchor(0.5f, 0.5f)
								.zIndex(10).extraInfo(b).icon(getIconForStep(step)));
					}
					// 最后路段绘制出口点
//					if ((i == stepss.size() - 1) &&  (step.getEndLocation() != null)) {
//						overlayOptionses.add((new MarkerOptions()).position(step.getEndLocation()).anchor(0.5f, 0.5f)
//								.zIndex(10).extraInfo(b).icon(getIconForStep(step)));
//					}
				}
			}

			
			// polyline
			if (stepss != null && stepss.size() > 0) {
				//List<MassTransitRouteLine.TransitStep> list = mRouteLine.getNewSteps().get(0);
				for (int i = 0; i < stepss.size(); i++) {
					MassTransitRouteLine.TransitStep step = stepss.get(i).get(0);
					if (step.getWayPoints() == null) {
						continue;
					}
					int color = 0;
					if (step.getVehileType() == StepVehicleInfoType.ESTEP_WALK) {
						color = getLineColor("walkLine");
					} else if (step.getVehileType() == StepVehicleInfoType.ESTEP_TRAIN) {
						color = getLineColor("subwayLine");
					} else if (step.getVehileType() == StepVehicleInfoType.ESTEP_BUS) {
						color = getLineColor("busLine");
					}

					int lineWidth = 1;
					if (step.getVehileType() == StepVehicleInfoType.ESTEP_WALK) {
						lineWidth = getLineWidth("walkLine");
					} else if (step.getVehileType() == StepVehicleInfoType.ESTEP_TRAIN) {
						lineWidth = getLineWidth("subwayLine");
					} else if (step.getVehileType() == StepVehicleInfoType.ESTEP_BUS) {
						lineWidth = getLineWidth("busLine");
					}

					boolean dash = false;
					if (step.getVehileType() == StepVehicleInfoType.ESTEP_WALK) {
						dash = dash("walkLine");
					} else if (step.getVehileType() == StepVehicleInfoType.ESTEP_TRAIN) {
						dash = dash("subwayLine");
					} else if (step.getVehileType() == StepVehicleInfoType.ESTEP_BUS) {
						dash = dash("busLine");
					}

					BitmapDescriptor textureImg = null;
					if (step.getVehileType() == StepVehicleInfoType.ESTEP_WALK) {
						textureImg = getTextureImg("walkLine");
					} else if (step.getVehileType() == StepVehicleInfoType.ESTEP_TRAIN) {
						textureImg = getTextureImg("subwayLine");
					} else if (step.getVehileType() == StepVehicleInfoType.ESTEP_BUS) {
						textureImg = getTextureImg("busLine");
					}

					overlayOptionses.add(new PolylineOptions().points(step.getWayPoints()).width(lineWidth).color(color)
							.dottedLine(dash).customTexture(textureImg).zIndex(0));
				}
			}
		}else {
            // 跨城 （跨城时，每个steps的get(i)对应的List是一条step的子路线sub_step，需要将它们全部拼接才是一条完整路线）
			int stepSum = 0;
            for (int i = 0; i < stepss.size(); i++ ) {
                stepSum +=  stepss.get(i).size();
            }

            // step node
            int k = 1;
            for ( int i = 0; i < stepss.size(); i++ ) {

                for (int j = 0; j < stepss.get(i).size(); j++ ) {
                    MassTransitRouteLine.TransitStep step = stepss.get(i).get(j);
                    Bundle b = new Bundle();
                    b.putInt("index", k);

                    if (step.getStartLocation() != null) {
                        overlayOptionses.add((new MarkerOptions()).position(step.getStartLocation())
                                .anchor(0.5f, 0.5f).zIndex(10).extraInfo(b).icon(getIconForStep(step)));
                    }

                    // 最后一个终点
                    if ( (k ==  stepSum ) &&  (step.getEndLocation() != null)) {
                        overlayOptionses.add((new MarkerOptions()).position(step.getEndLocation())
                                .anchor(0.5f, 0.5f).zIndex(10).icon(getIconForStep(step)));
                    }

                    k++;
                }
            }


            // polyline
            for ( int i = 0; i < stepss.size(); i++ ) {
                for (int j = 0; j < stepss.get(i).size(); j++ ) {
                    MassTransitRouteLine.TransitStep step = stepss.get(i).get(j);
                    int color = 0;
					if (step.getVehileType() == StepVehicleInfoType.ESTEP_WALK) {
						color = getLineColor("walkLine");
					} else if (step.getVehileType() == StepVehicleInfoType.ESTEP_TRAIN) {
						color = getLineColor("subwayLine");
					} else if (step.getVehileType() == StepVehicleInfoType.ESTEP_BUS) {
						color = getLineColor("busLine");
					}
					int lineWidth = 1;
					if (step.getVehileType() == StepVehicleInfoType.ESTEP_WALK) {
						lineWidth = getLineWidth("walkLine");
					} else if (step.getVehileType() == StepVehicleInfoType.ESTEP_TRAIN) {
						lineWidth = getLineWidth("subwayLine");
					} else if (step.getVehileType() == StepVehicleInfoType.ESTEP_BUS) {
						lineWidth = getLineWidth("busLine");
					}
					boolean dash = false;
					if (step.getVehileType() == StepVehicleInfoType.ESTEP_WALK) {
						dash = dash("walkLine");
					} else if (step.getVehileType() == StepVehicleInfoType.ESTEP_TRAIN) {
						dash = dash("subwayLine");
					} else if (step.getVehileType() == StepVehicleInfoType.ESTEP_BUS) {
						dash = dash("busLine");
					}
					BitmapDescriptor textureImg = null;
					if (step.getVehileType() == StepVehicleInfoType.ESTEP_WALK) {
						textureImg = getTextureImg("walkLine");
					} else if (step.getVehileType() == StepVehicleInfoType.ESTEP_TRAIN) {
						textureImg = getTextureImg("subwayLine");
					} else if (step.getVehileType() == StepVehicleInfoType.ESTEP_BUS) {
						textureImg = getTextureImg("busLine");
					}
                    if (step.getWayPoints() != null ) {
                        overlayOptionses.add(new PolylineOptions()
                                            .points(step.getWayPoints()).width(lineWidth).color(color).dottedLine(dash).customTexture(textureImg)
                                            .zIndex(0));
                    }
                }
            }
		}
		
		if (mRouteLine.getStarting() != null) {
			overlayOptionses.add((new MarkerOptions()).position(mRouteLine.getStarting().getLocation())
					.icon(getStartMarker() != null ? getStartMarker()
							: BitmapDescriptorFactory.fromAssetWithDpi("Icon_start.png"))
					.zIndex(10));
		}
		if (mRouteLine.getTerminal() != null) {
			overlayOptionses.add((new MarkerOptions()).position(mRouteLine.getTerminal().getLocation())
					.icon(getTerminalMarker() != null ? getTerminalMarker()
							: BitmapDescriptorFactory.fromAssetWithDpi("Icon_end.png"))
					.zIndex(10));
		}

		return overlayOptionses;
	}

	private BitmapDescriptor getIconForStep(MassTransitRouteLine.TransitStep step) {
		switch (step.getVehileType()) {
		case ESTEP_BUS:
			return getBusMarker();
		case ESTEP_TRAIN:
			return getTrainMarker();
		case ESTEP_WALK:
			return getWalkMarker();
		default:
			return null;
		}
	}

	public BitmapDescriptor getBusMarker() {
		return null;
	}

	public BitmapDescriptor getTrainMarker() {
		return null;
	}

	public BitmapDescriptor getWalkMarker() {
		return null;
	}

	public int getLineWidth(String node) {
		return 3;
	}

	public boolean dash(String node) {
		return false;
	}

	public BitmapDescriptor getTextureImg(String node) {
		return null;
	}

	public boolean isSameCity() {
		return false;
	}

	/**
	 * 设置路线数据
	 * 
	 * @param routeOverlay
	 *            路线数据
	 */
	public void setData(MassTransitRouteLine routeOverlay) {
		this.mRouteLine = routeOverlay;

	}

	/**
	 * 覆写此方法以改变默认起点图标
	 * 
	 * @return 起点图标
	 */
	public BitmapDescriptor getStartMarker() {
		return null;
	}

	/**
	 * 覆写此方法以改变默认终点图标
	 * 
	 * @return 终点图标
	 */
	public BitmapDescriptor getTerminalMarker() {
		return null;
	}

	public int getLineColor(String node) {
		return 0;
	}

	/**
	 * 覆写此方法以改变起默认点击行为
	 * 
	 * @param i
	 *            被点击的step在
	 *            {@link com.baidu.mapapi.search.route.TransitRouteLine#getAllStep()}
	 *            中的索引
	 * @return 是否处理了该点击事件
	 */
	public boolean onRouteNodeClick(int i) {
		if (mRouteLine.getAllStep() != null && mRouteLine.getAllStep().get(i) != null) {
			Log.i("baidumapsdk", "TransitRouteOverlay onRouteNodeClick");
		}
		return false;
	}

	@Override
	public final boolean onMarkerClick(Marker marker) {
		for (Overlay mMarker : mOverlayList) {
			if (mMarker instanceof Marker && mMarker.equals(marker)) {
				if (marker.getExtraInfo() != null) {
					onRouteNodeClick(marker.getExtraInfo().getInt("index"));
				}
			}
		}
		return true;
	}

	@Override
	public boolean onPolylineClick(Polyline polyline) {
		return false;
	}
}
