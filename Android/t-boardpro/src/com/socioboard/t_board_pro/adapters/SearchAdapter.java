package com.socioboard.t_board_pro.adapters;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.socioboard.t_board_pro.AnyUserProfileDialog;
import com.socioboard.t_board_pro.MainActivity;
import com.socioboard.t_board_pro.lazylist.ImageLoader;
import com.socioboard.t_board_pro.twitterapi.TwitterPostRequestFollow;
import com.socioboard.t_board_pro.twitterapi.TwitterPostRequestUnFollow;
import com.socioboard.t_board_pro.twitterapi.TwitterRequestCallBack;
import com.socioboard.t_board_pro.util.MainSingleTon;
import com.socioboard.t_board_pro.util.ModelUserDatas;
import com.socioboard.t_board_pro.util.TboardproLocalData;
import com.socioboard.t_board_pro.util.ToFollowingModel;
import com.socioboard.tboardpro.R;

public class SearchAdapter extends BaseAdapter {

	public Context context;

	int completesCount = 0, allCount = 0, allSize;

	public ArrayList<ToFollowingModel> tweetModels;

	Activity activity;

	ImageLoader imageLoader;

	ProgressDialog progressDialog;

	public SparseBooleanArray sparseBooleanArray;

	public SearchAdapter(Context context,
			ArrayList<ToFollowingModel> tweetModels, Activity activity) {

		this.context = context;

		this.tweetModels = tweetModels;

		imageLoader = new ImageLoader(context);

		this.activity = activity;

		activity.runOnUiThread(new Runnable() {

			@Override
			public void run() {

				progressDialog = new ProgressDialog(SearchAdapter.this.context);
				progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				progressDialog.setIndeterminate(true);
				progressDialog.setCancelable(false);
			}
		});

		sparseBooleanArray = new SparseBooleanArray(tweetModels.size());

		for (int i = 0; i < tweetModels.size(); ++i) {

			sparseBooleanArray.put(i, false);

		}
	}

	@Override
	public int getCount() {
		return tweetModels.size();
	}

	@Override
	public ToFollowingModel getItem(int position) {
		return tweetModels.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		if (convertView == null) {

			LayoutInflater mInflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = mInflater
					.inflate(R.layout.search_item, parent, false);
		}

		final ToFollowingModel toFollowingModel = getItem(position);

		ImageView profilePic = (ImageView) convertView
				.findViewById(R.id.profile_pic);
		profilePic.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				String newUser = toFollowingModel.getUserName();

				newUser = newUser.replace("@", "");

				if (newUser.equals(MainSingleTon.currentUserModel.getUsername())) {
					return;
				}

				AnyUserProfileDialog anyUserProfile = new AnyUserProfileDialog(
						activity, newUser, toFollowingModel.getId());

			}
		});

		TextView userName = (TextView) convertView
				.findViewById(R.id.followerName);

		userName.setText(toFollowingModel.getUserName());

		userName.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				String newUser = toFollowingModel.getUserName();

				newUser = newUser.replace("@", "");

				if (newUser.equals(MainSingleTon.currentUserModel.getUsername())) {
					return;
				}
				AnyUserProfileDialog anyUserProfile = new AnyUserProfileDialog(
						activity, newUser, toFollowingModel.getId());

			}
		});
		TextView noOfTweets = (TextView) convertView
				.findViewById(R.id.textViewNotweets);

		noOfTweets.setText(toFollowingModel.getNoTweets());

		TextView noOfFollowings = (TextView) convertView
				.findViewById(R.id.textViewNoOfFollowings);

		noOfFollowings.setText(toFollowingModel.getNoToFollowing());

		TextView noOfFollowers = (TextView) convertView
				.findViewById(R.id.textViewNoofFollowers);

		noOfFollowers.setText(toFollowingModel.getNoFollowers());

		final Button buttonFollow = (Button) convertView
				.findViewById(R.id.buttonFollow);

		final Button button1FollowByAll = (Button) convertView
				.findViewById(R.id.button1FollowByAll);

		final Button buttonUnfollow = (Button) convertView
				.findViewById(R.id.buttonUnfollow);

		if (toFollowingModel.isFollowingStatus()) {

			buttonFollow.setVisibility(View.INVISIBLE);

			buttonUnfollow.setVisibility(View.VISIBLE);

		} else {

			buttonFollow.setVisibility(View.VISIBLE);

			buttonUnfollow.setVisibility(View.INVISIBLE);

		}

		final int pos = position;

		buttonFollow.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				myprint("buttonFollow " + getItem(pos));

				progressDialog.setMessage(getItem(pos).getUserName()
						+ " Following...");
				progressDialog.show();

				TwitterPostRequestFollow twitterPostRequestFollow = new TwitterPostRequestFollow(
						MainSingleTon.currentUserModel,
						new TwitterRequestCallBack() {

							@Override
							public void onSuccess(JSONObject jsonObject) {

							}

							@Override
							public void onSuccess(String jsonResult) {

								hideProgress();

								myprint("buttonFollow onSuccess");

								activity.runOnUiThread(new Runnable() {

									@Override
									public void run() {

										buttonFollow.setVisibility(View.INVISIBLE);

										buttonUnfollow.setVisibility(View.VISIBLE);
										
										++MainSingleTon.followingCount;
										MainSingleTon.toFollowingModelsIDs.add(getItem(pos).getId());

										MainActivity.isNeedToRefreshDrawer = true;

									}
								});

								getItem(pos).setFollowingStatus(true);

							}

							@Override
							public void onFailure(Exception e) {

								myprint("buttonFollow onFailure" + e);

								hideProgress();
							}
						});

				twitterPostRequestFollow.executeThisRequest(getItem(pos)
						.getId());

			}
		});

		buttonUnfollow.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				myprint("buttonUnfollow " + getItem(pos));
				progressDialog.setMessage(getItem(pos).getUserName()
						+ " UnFollowing...");

				showProgress();

				TwitterPostRequestUnFollow twitterPostRequestUnFollow = new TwitterPostRequestUnFollow(
						MainSingleTon.currentUserModel,
						new TwitterRequestCallBack() {

							@Override
							public void onSuccess(JSONObject jsonObject) {

							}

							@Override
							public void onSuccess(String jsonResult) {

								myprint("buttonUnfollow onSuccess");

								hideProgress();

								activity.runOnUiThread(new Runnable() {

									@Override
									public void run() {

										new Handler().post(new Runnable() {

											@Override
											public void run() {

												buttonFollow
														.setVisibility(View.VISIBLE);

												buttonUnfollow
														.setVisibility(View.INVISIBLE);
												
												++MainSingleTon.followingCount;
												
												MainSingleTon.toFollowingModelsIDs.remove(getItem(pos).getId());

												MainActivity.isNeedToRefreshDrawer = true;

											}
										});
									}
								});

								getItem(pos).setFollowingStatus(false);

							}

							@Override
							public void onFailure(Exception e) {

								hideProgress();
								myprint("buttonUnfollow onFailure" + e);
							}
						});

				twitterPostRequestUnFollow.executeThisRequest(getItem(pos)
						.getId());

			}
		});

		imageLoader.DisplayImage(toFollowingModel.getUserImagerUrl(),
				profilePic);

		if (sparseBooleanArray.get(pos)) {

			button1FollowByAll.setText("Followed By All");

		}

		button1FollowByAll.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				myprint("buttonFollow " + getItem(pos));

				TboardproLocalData localData = new TboardproLocalData(context);

				List<ModelUserDatas> listDatas = localData
						.getAllUsersDataArlist();

				completesCount = 0;

				allCount = 0;

				allSize = listDatas.size();

				progressDialog.setMessage(getItem(pos).getUserName()
						+ " Following by..." + allSize);

				progressDialog.show();

				for (int i = 0; i < listDatas.size(); i++) {

					TwitterPostRequestFollow twitterPostRequestFollow = new TwitterPostRequestFollow(
							listDatas.get(i), new TwitterRequestCallBack() {

								@Override
								public void onSuccess(JSONObject jsonObject) {

								}

								@Override
								public void onSuccess(String jsonResult) {

									myprint("buttonFollow onSuccess");

									++completesCount;

									getItem(pos).setFollowingStatus(true);

									++allCount;

									if (allSize <= allCount) {

										hideProgress();
									}

									activity.runOnUiThread(new Runnable() {

										@Override
										public void run() {

											buttonFollow
													.setVisibility(View.INVISIBLE);

											buttonUnfollow
													.setVisibility(View.VISIBLE);

											button1FollowByAll
													.setText("Followed By All");
											sparseBooleanArray.put(pos, true);
										}
									});
								}

								@Override
								public void onFailure(Exception e) {

									myprint("buttonFollow onFailure" + e);

									++allCount;

								}
							});

					twitterPostRequestFollow.executeThisRequest(getItem(pos)
							.getId());
				}
			}
		});

		return convertView;
	}

	public void myprint(Object msg) {

		System.out.println(msg.toString());

	}

	void showProgress() {

		activity.runOnUiThread(new Runnable() {

			@Override
			public void run() {

				progressDialog.show();

			}
		});
	}

	void hideProgress() {

		activity.runOnUiThread(new Runnable() {

			@Override
			public void run() {

				progressDialog.cancel();

			}
		});
	}

}
