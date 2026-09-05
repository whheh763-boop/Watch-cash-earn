package com.example.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.applovin.sdk.AppLovinSdk
import com.applovin.sdk.AppLovinSdkConfiguration
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.MaxReward
import com.applovin.mediation.MaxRewardedAdListener
import com.applovin.mediation.ads.MaxInterstitialAd
import com.applovin.mediation.ads.MaxRewardedAd
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.unity3d.ads.IUnityAdsInitializationListener
import com.unity3d.ads.IUnityAdsLoadListener
import com.unity3d.ads.IUnityAdsShowListener
import com.unity3d.ads.UnityAds
import com.unity3d.ads.UnityAdsShowOptions

object AdsManager {
    private const val TAG = "AdsManager"

    // AdMob Real Ad Unit IDs
    private const val ADMOB_INTERSTITIAL_ID = "ca-app-pub-8551073579787342/9478726696" // Using Rewarded Interstitial ID for interstitial slot per request
    private const val ADMOB_REWARDED_ID = "ca-app-pub-8551073579787342/1051396163"
    private const val ADMOB_BANNER_ID = "ca-app-pub-8551073579787342/9889790237"
    private const val ADMOB_APP_OPEN_ID = "ca-app-pub-8551073579787342/9590407330"
    private const val ADMOB_NATIVE_ID = "ca-app-pub-8551073579787342/6222007967"

    // AppLovin MAX IDs (Placeholders)
    private const val APPLOVIN_INTERSTITIAL_ID = "YOUR_MAX_INTERSTITIAL_AD_UNIT_ID"
    private const val APPLOVIN_REWARDED_ID = "YOUR_MAX_REWARDED_AD_UNIT_ID"

    // Unity Ads Game ID
    // Use the official Unity Test ID for Android
    private const val UNITY_GAME_ID = "4920257"

    private var admobInterstitialAd: InterstitialAd? = null
    private var admobRewardedAd: RewardedAd? = null

    private var applovinInterstitialAd: MaxInterstitialAd? = null
    private var applovinRewardedAd: MaxRewardedAd? = null

    private var isAdmobInitialized = false
    private var isAppLovinInitialized = false
    private var isUnityInitialized = false

    fun initialize(activity: Activity) {
        // 1. Initialize AdMob
        MobileAds.initialize(activity) { initializationStatus ->
            isAdmobInitialized = true
            Log.d(TAG, "AdMob Initialized: ${initializationStatus.adapterStatusMap}")
            // Start preloading ads after initialization
            loadAdMobInterstitial(activity)
            loadAdMobRewarded(activity)
        }

        // 2. Initialize AppLovin (Disabled - Requires valid SDK Key to prevent 404/network errors)
        /*
        AppLovinSdk.getInstance(activity).settings.setVerboseLogging(true)
        AppLovinSdk.getInstance(activity).initializeSdk { configuration: AppLovinSdkConfiguration ->
            isAppLovinInitialized = true
            Log.d(TAG, "AppLovin Initialized")
            loadAppLovinInterstitial(activity)
            loadAppLovinRewarded(activity)
        }
        */

        // 3. Initialize Unity Ads (Disabled - Using AdMob primarily for now)
        /*
        UnityAds.initialize(activity, UNITY_GAME_ID, true, object : IUnityAdsInitializationListener {
            override fun onInitializationComplete() {
                isUnityInitialized = true
                Log.d(TAG, "Unity Ads Initialized successfully")
                loadUnityAds()
            }

            override fun onInitializationFailed(error: UnityAds.UnityAdsInitializationError?, message: String?) {
                Log.e(TAG, "Unity Ads Initialization Failed: $error - $message")
            }
        })
        */
    }

    // --- AdMob Methods ---

    fun loadAdMobInterstitial(context: Context) {
        if (admobInterstitialAd != null) return
        
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(context, ADMOB_INTERSTITIAL_ID, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(ad: InterstitialAd) {
                Log.d(TAG, "AdMob Interstitial Loaded")
                admobInterstitialAd = ad
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                Log.e(TAG, "AdMob Interstitial Failed to load: ${error.message}")
                admobInterstitialAd = null
            }
        })
    }

    fun showAdMobInterstitial(activity: Activity, onAdDismissed: () -> Unit) {
        if (admobInterstitialAd != null) {
            admobInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "AdMob Interstitial Dismissed")
                    admobInterstitialAd = null
                    loadAdMobInterstitial(activity)
                    onAdDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    Log.e(TAG, "AdMob Interstitial Failed to show: ${error.message}")
                    admobInterstitialAd = null
                    onAdDismissed()
                }
            }
            admobInterstitialAd?.show(activity)
        } else {
            Log.w(TAG, "AdMob Interstitial not ready yet")
            onAdDismissed()
            loadAdMobInterstitial(activity)
        }
    }

    fun loadAdMobRewarded(context: Context) {
        if (admobRewardedAd != null) return

        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(context, ADMOB_REWARDED_ID, adRequest, object : RewardedAdLoadCallback() {
            override fun onAdLoaded(ad: RewardedAd) {
                Log.d(TAG, "AdMob Rewarded Loaded")
                admobRewardedAd = ad
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                Log.e(TAG, "AdMob Rewarded Failed to load: ${error.message}")
                admobRewardedAd = null
            }
        })
    }

    fun showAdMobRewarded(activity: Activity, onRewardEarned: () -> Unit, onAdDismissed: () -> Unit) {
        if (admobRewardedAd != null) {
            admobRewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "AdMob Rewarded Dismissed")
                    admobRewardedAd = null
                    loadAdMobRewarded(activity)
                    onAdDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    Log.e(TAG, "AdMob Rewarded Failed to show: ${error.message}")
                    admobRewardedAd = null
                    onAdDismissed()
                }
            }
            
            admobRewardedAd?.show(activity) { rewardItem ->
                Log.d(TAG, "User earned reward: ${rewardItem.amount} ${rewardItem.type}")
                onRewardEarned()
            }
        } else {
            Log.w(TAG, "AdMob Rewarded not ready yet")
            onAdDismissed()
            loadAdMobRewarded(activity)
        }
    }

    // --- AppLovin Methods ---

    fun loadAppLovinInterstitial(activity: Activity) {
        applovinInterstitialAd = MaxInterstitialAd(APPLOVIN_INTERSTITIAL_ID, activity)
        applovinInterstitialAd?.setListener(object : MaxAdListener {
            override fun onAdLoaded(ad: MaxAd) { Log.d(TAG, "AppLovin Interstitial Loaded") }
            override fun onAdLoadFailed(adUnitId: String, error: MaxError) { Log.e(TAG, "AppLovin Interstitial Failed: ${error.message}") }
            override fun onAdDisplayFailed(ad: MaxAd, error: MaxError) { loadAppLovinInterstitial(activity) }
            override fun onAdDisplayed(ad: MaxAd) {}
            override fun onAdClicked(ad: MaxAd) {}
            override fun onAdHidden(ad: MaxAd) { loadAppLovinInterstitial(activity) }
        })
        applovinInterstitialAd?.loadAd()
    }

    fun showAppLovinInterstitial(activity: Activity) {
        if (applovinInterstitialAd?.isReady == true) {
            applovinInterstitialAd?.showAd()
        } else {
            loadAppLovinInterstitial(activity)
        }
    }

    fun loadAppLovinRewarded(activity: Activity) {
        applovinRewardedAd = MaxRewardedAd.getInstance(APPLOVIN_REWARDED_ID, activity)
        applovinRewardedAd?.setListener(object : MaxRewardedAdListener {
            override fun onAdLoaded(ad: MaxAd) { Log.d(TAG, "AppLovin Rewarded Loaded") }
            override fun onAdLoadFailed(adUnitId: String, error: MaxError) { Log.e(TAG, "AppLovin Rewarded Failed: ${error.message}") }
            override fun onAdDisplayFailed(ad: MaxAd, error: MaxError) { loadAppLovinRewarded(activity) }
            override fun onAdDisplayed(ad: MaxAd) {}
            override fun onAdClicked(ad: MaxAd) {}
            override fun onAdHidden(ad: MaxAd) { loadAppLovinRewarded(activity) }
            override fun onUserRewarded(ad: MaxAd, reward: MaxReward) { Log.d(TAG, "AppLovin User Rewarded") }
        })
        applovinRewardedAd?.loadAd()
    }

    fun showAppLovinRewarded(activity: Activity) {
        if (applovinRewardedAd?.isReady == true) {
            applovinRewardedAd?.showAd()
        } else {
            loadAppLovinRewarded(activity)
        }
    }

    // --- Unity Ads Methods ---

    fun loadUnityAds() {
        UnityAds.load("Interstitial_Android", object : IUnityAdsLoadListener {
            override fun onUnityAdsAdLoaded(placementId: String) {
                Log.d(TAG, "Unity Ad Loaded for placement: $placementId")
            }
            override fun onUnityAdsFailedToLoad(placementId: String, error: UnityAds.UnityAdsLoadError, message: String) {
                Log.e(TAG, "Unity Ad Failed to Load: $placementId - $message")
            }
        })
    }
    
    fun showUnityInterstitial(activity: Activity, onAdDismissed: () -> Unit) {
        UnityAds.show(activity, "Interstitial_Android", UnityAdsShowOptions(), object : IUnityAdsShowListener {
            override fun onUnityAdsShowFailure(placementId: String, error: UnityAds.UnityAdsShowError, message: String) {
                Log.e(TAG, "Unity Ad Show Failure: $placementId - $message")
                onAdDismissed()
            }
            override fun onUnityAdsShowStart(placementId: String) {}
            override fun onUnityAdsShowClick(placementId: String) {}
            override fun onUnityAdsShowComplete(placementId: String, state: UnityAds.UnityAdsShowCompletionState) {
                Log.d(TAG, "Unity Ad Show Complete: $placementId state: $state")
                onAdDismissed()
                loadUnityAds() // Preload next
            }
        })
    }
}
