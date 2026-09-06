sed -i 's/onNavigateToVideo: () -> Unit/onNavigateToVideo: () -> Unit,\n    onNavigateToRefer: () -> Unit,\n    onNavigateToLeaderboard: () -> Unit/g' app/src/main/java/com/example/ui/screens/HomeScreen.kt

sed -i 's/onNavigateToVideo = { navController.navigate("watch_video") }/onNavigateToVideo = { navController.navigate("watch_video") },\n                        onNavigateToRefer = { navController.navigate("refer") },\n                        onNavigateToLeaderboard = { navController.navigate("leaderboard") }/g' app/src/main/java/com/example/MainActivity.kt
