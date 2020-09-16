package ru.skillbranch.skillarticles

import android.Manifest
import android.net.Uri
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.testing.launchFragment
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import androidx.test.ext.junit.runners.AndroidJUnit4
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.internal.notify
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.*
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import ru.skillbranch.skillarticles.data.local.PrefManager
import ru.skillbranch.skillarticles.data.models.User
import ru.skillbranch.skillarticles.data.repositories.ProfileRepository
import ru.skillbranch.skillarticles.ui.RootActivity
import ru.skillbranch.skillarticles.ui.profile.ProfileFragment
import ru.skillbranch.skillarticles.viewmodels.profile.PendingAction
import ru.skillbranch.skillarticles.viewmodels.profile.ProfileViewModel
import java.io.FileNotFoundException


val avatarUploadRes: String = """{
    "url":"https://skill-branch.ru/avatar.jpg"
    }
""".trimIndent()

val avatarRemoveRes: String = """{
    "url":""
    }
""".trimIndent()

val profileEditRes: String = """{
    "id": "test_id",
    "name": "edit test name",
    "avatar": "https://skill-branch.ru/avatar.jpg",
    "rating": 0,
    "respect": 0,
    "about": "edit something about"
    }
""".trimIndent()

@RunWith(AndroidJUnit4::class)
class InstrumentalTest1 {
    private lateinit var server: MockWebServer
    private val profileRepository = ProfileRepository

    @Captor
    private lateinit var argCaptor: ArgumentCaptor<Map<String, Pair<Boolean, Boolean>>>

    companion object {
        @BeforeClass
        @JvmStatic
        fun setup() {
            AppConfig.BASE_URL = "http://localhost:8080/"
        }
    }

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this);
        server = MockWebServer()
        server.start(8080)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun upload_avatar() {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(avatarUploadRes)
        )

        runBlocking {
            PrefManager.accessToken = "Bearer test_access_token"
            val reqFile: RequestBody = "test".toRequestBody("image/jpeg".toMediaType())
            val body: MultipartBody.Part =
                MultipartBody.Part.createFormData("avatar", "name.jpg", reqFile)
            profileRepository.uploadAvatar(body)

            Assert.assertEquals("https://skill-branch.ru/avatar.jpg", PrefManager.profile?.avatar)

            val recordedRequest = server.takeRequest();

            Assert.assertEquals("POST", recordedRequest.method)
            Assert.assertEquals("/profile/avatar/upload", recordedRequest.path)
            Assert.assertEquals(
                "Bearer test_access_token",
                recordedRequest.headers["Authorization"]
            )
            Assert.assertEquals(218, recordedRequest.body.size)
        }
    }

    @Test
    fun remove_avatar() {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(avatarRemoveRes)
        )
        runBlocking {
            PrefManager.accessToken = "Bearer test_access_token"
            profileRepository.removeAvatar()

            Assert.assertEquals("", PrefManager.profile?.avatar)

            val recordedRequest = server.takeRequest();

            Assert.assertEquals("PUT", recordedRequest.method)
            Assert.assertEquals("/profile/avatar/remove", recordedRequest.path)
            Assert.assertEquals(
                "Bearer test_access_token",
                recordedRequest.headers["Authorization"]
            )
        }
    }

    @Test
    fun edit_profile() {
        val expectedUser = User(
            id = "test_id",
            name = "test name",
            avatar = "https://skill-branch.ru/avatar.jpg",
            rating = 0,
            respect = 0,
            about = "something about"
        )
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(profileEditRes)
        )

        runBlocking {
            PrefManager.accessToken = "Bearer test_access_token"
            PrefManager.profile = expectedUser
            Assert.assertEquals(expectedUser, PrefManager.profile)

            profileRepository.editProfile("edit test name", "edit something about")
            Assert.assertEquals(
                expectedUser.copy(
                    name = "edit test name",
                    about = "edit something about"
                ), PrefManager.profile
            )
            val recordedRequest = server.takeRequest();

            Assert.assertEquals("PUT", recordedRequest.method)
            Assert.assertEquals("/profile", recordedRequest.path)
            Assert.assertEquals(
                "Bearer test_access_token",
                recordedRequest.headers["Authorization"]
            )
            Assert.assertEquals(
                "[text={\"name\":\"edit test name\",\"about\":\"edit something about\"}]",
                recordedRequest.body.toString()
            )
        }
    }

    /*
   Started running tests


java.lang.RuntimeException: java.lang.NullPointerException: Attempt to invoke virtual method 'boolean android.app.ActivityManager.isLowRamDevice()' on a null object reference
at androidx.test.runner.MonitoringInstrumentation.runOnMainSync(MonitoringInstrumentation.java:441)
at androidx.test.core.app.ActivityScenario.onActivity(ActivityScenario.java:564)
at androidx.fragment.app.testing.FragmentScenario.internalLaunch(FragmentScenario.java:300)
at androidx.fragment.app.testing.FragmentScenario.launch(FragmentScenario.java:213)
at ru.skillbranch.skillarticles.InstrumentalTest1.prepare_and_delete_uri(InstrumentalTest1.kt:518)
at java.lang.reflect.Method.invoke(Native Method)
at org.junit.runners.model.FrameworkMethod$1.runReflectiveCall(FrameworkMethod.java:59)
at org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:12)
at org.junit.runners.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java:56)
at org.junit.internal.runners.statements.InvokeMethod.evaluate(InvokeMethod.java:17)
at androidx.test.internal.runner.junit4.statement.RunBefores.evaluate(RunBefores.java:80)
at androidx.test.internal.runner.junit4.statement.RunAfters.evaluate(RunAfters.java:61)
at org.junit.runners.ParentRunner$3.evaluate(ParentRunner.java:306)
at org.junit.runners.BlockJUnit4ClassRunner$1.evaluate(BlockJUnit4ClassRunner.java:100)
at org.junit.runners.ParentRunner.runLeaf(ParentRunner.java:366)
at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:103)
at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:63)
at org.junit.runners.ParentRunner$4.run(ParentRunner.java:331)
at org.junit.runners.ParentRunner$1.schedule(ParentRunner.java:79)
at org.junit.runners.ParentRunner.runChildren(ParentRunner.java:329)
at org.junit.runners.ParentRunner.access$100(ParentRunner.java:66)
at org.junit.runners.ParentRunner$2.evaluate(ParentRunner.java:293)
at org.junit.internal.runners.statements.RunBefores.evaluate(RunBefores.java:26)
at org.junit.runners.ParentRunner$3.evaluate(ParentRunner.java:306)
at org.junit.runners.ParentRunner.run(ParentRunner.java:413)
at androidx.test.ext.junit.runners.AndroidJUnit4.run(AndroidJUnit4.java:104)
at org.junit.runners.Suite.runChild(Suite.java:128)
at org.junit.runners.Suite.runChild(Suite.java:27)
at org.junit.runners.ParentRunner$4.run(ParentRunner.java:331)
at org.junit.runners.ParentRunner$1.schedule(ParentRunner.java:79)
at org.junit.runners.ParentRunner.runChildren(ParentRunner.java:329)
at org.junit.runners.ParentRunner.access$100(ParentRunner.java:66)
at org.junit.runners.ParentRunner$2.evaluate(ParentRunner.java:293)
at org.junit.runners.ParentRunner$3.evaluate(ParentRunner.java:306)
at org.junit.runners.ParentRunner.run(ParentRunner.java:413)
at org.junit.runner.JUnitCore.run(JUnitCore.java:137)
at org.junit.runner.JUnitCore.run(JUnitCore.java:115)
at androidx.test.internal.runner.TestExecutor.execute(TestExecutor.java:56)
at androidx.test.runner.AndroidJUnitRunner.onStart(AndroidJUnitRunner.java:392)
at android.app.Instrumentation$InstrumentationThread.run(Instrumentation.java:2074)
Caused by: java.lang.NullPointerException: Attempt to invoke virtual method 'boolean android.app.ActivityManager.isLowRamDevice()' on a null object reference
at com.bumptech.glide.load.engine.cache.MemorySizeCalculator.isLowMemoryDevice(MemorySizeCalculator.java:119)
at com.bumptech.glide.load.engine.cache.MemorySizeCalculator$Builder.<init>(MemorySizeCalculator.java:169)
at com.bumptech.glide.GlideBuilder.build(GlideBuilder.java:515)
at com.bumptech.glide.Glide.initializeGlide(Glide.java:290)
at com.bumptech.glide.Glide.initializeGlide(Glide.java:242)
at com.bumptech.glide.Glide.checkAndInitializeGlide(Glide.java:201)
at com.bumptech.glide.Glide.get(Glide.java:182)
at com.bumptech.glide.Glide.getRetriever(Glide.java:749)
at com.bumptech.glide.Glide.with(Glide.java:801)
at ru.skillbranch.skillarticles.ui.profile.ProfileFragment.updateAvatar(ProfileFragment.kt:152)
at ru.skillbranch.skillarticles.ui.profile.ProfileFragment.access$updateAvatar(ProfileFragment.kt:44)
at ru.skillbranch.skillarticles.ui.profile.ProfileFragment$ProfileBinding$avatar$2.invoke(ProfileFragment.kt:252)
at ru.skillbranch.skillarticles.ui.profile.ProfileFragment$ProfileBinding$avatar$2.invoke(ProfileFragment.kt:250)
at ru.skillbranch.skillarticles.ui.delegates.RenderProp.bind(RenderProp.kt:15)
at ru.skillbranch.skillarticles.ui.base.Binding.rebind(Binding.kt:21)
at ru.skillbranch.skillarticles.ui.base.BaseFragment.onViewStateRestored(BaseFragment.kt:78)
at androidx.fragment.app.Fragment.restoreViewState(Fragment.java:631)
at androidx.fragment.app.Fragment.restoreViewState(Fragment.java:2982)
at androidx.fragment.app.Fragment.performActivityCreated(Fragment.java:2973)
at androidx.fragment.app.FragmentStateManager.activityCreated(FragmentStateManager.java:597)
at androidx.fragment.app.FragmentStateManager.moveToExpectedState(FragmentStateManager.java:279)
at androidx.fragment.app.FragmentStore.moveToExpectedState(FragmentStore.java:112)
at androidx.fragment.app.FragmentManager.moveToState(FragmentManager.java:1632)
at androidx.fragment.app.BackStackRecord.executeOps(BackStackRecord.java:455)
at androidx.fragment.app.FragmentManager.executeOps(FragmentManager.java:2389)
at androidx.fragment.app.FragmentManager.executeOpsTogether(FragmentManager.java:2145)
at androidx.fragment.app.FragmentManager.removeRedundantOperationsAndExecute(FragmentManager.java:2083)
at androidx.fragment.app.FragmentManager.execSingleAction(FragmentManager.java:1954)
at androidx.fragment.app.BackStackRecord.commitNow(BackStackRecord.java:300)
at androidx.fragment.app.testing.FragmentScenario$1.perform(FragmentScenario.java:317)
at androidx.fragment.app.testing.FragmentScenario$1.perform(FragmentScenario.java:301)
at androidx.test.core.app.ActivityScenario.lambda$onActivity$2$ActivityScenario(ActivityScenario.java:551)
at androidx.test.core.app.ActivityScenario$$Lambda$4.run(Unknown Source:4)
at java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:457)
at java.util.concurrent.FutureTask.run(FutureTask.java:266)
at android.app.Instrumentation$SyncRunnable.run(Instrumentation.java:2092)
at android.os.Handler.handleCallback(Handler.java:789)
at android.os.Handler.dispatchMessage(Handler.java:98)
at android.os.Looper.loop(Looper.java:164)
at android.app.ActivityThread.main(ActivityThread.java:6541)
at java.lang.reflect.Method.invoke(Native Method)
at com.android.internal.os.Zygote$MethodAndArgsCaller.run(Zygote.java:240)
at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:767)

Test running failed: Instrumentation run failed due to 'Process crashed.'
Tests ran to completion.


     */

    @Test
    fun prepare_and_delete_uri() {
        val expectedPath =
            "content://ru.skillbranch.skillarticles.provider/external_files/Android/data/ru.skillbranch.skillarticles/files/Pictures"
        val mockRoot = mock(
            RootActivity::class.java,
            withSettings().defaultAnswer(RETURNS_DEEP_STUBS)
        )

        with(launchFragment(themeResId = R.style.AppTheme) {
            ProfileFragment(mockRoot)
        }) {
            onFragment { fragment ->
                val uri = fragment.prepareTempUri()
                assertEquals(expectedPath, uri.toString().split("/").dropLast(1).joinToString("/"))
                fragment.removeTempUri(uri)
                try {
                    fragment.requireContext().contentResolver.openInputStream(uri)
                }catch (e: Throwable){
                    assertEquals(true, e is FileNotFoundException)
                }

            }
        }
    }

    @Test
    fun request_permissions() {
        val expectedResult = mapOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE to Pair(true , true),
            Manifest.permission.READ_EXTERNAL_STORAGE to Pair(true , true)
        )
        val testRegistry = object : ActivityResultRegistry() {
            override fun <I, O> onLaunch(
                requestCode: Int,
                contract: ActivityResultContract<I, O>,
                input: I,
                options: ActivityOptionsCompat?
            ) {
                dispatchResult(requestCode, expectedResult.mapValues { true })
            }
        }
        val mockRoot = mock(
            RootActivity::class.java,
            withSettings().defaultAnswer(RETURNS_DEEP_STUBS)
        )

        with(launchFragment(themeResId = R.style.AppTheme) {
            ProfileFragment(mockRoot, testRegistry, { MockViewModelFactory(it)} )
        }) {
            onFragment { fragment ->
                // Trigger the ActivityResultLauncher
                fragment.viewModel.requestPermissions(expectedResult.keys.toList())
                // Verify the result is set
                verify(fragment.viewModel).handlePermission(capture(argCaptor))

                assertEquals(expectedResult, argCaptor.value)
                verify(fragment.viewModel).executePendingAction()

            }
        }

    }

    @Test
    fun request_no_permissions() {
        val expectedResult = mapOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE to Pair(false , false),
            Manifest.permission.READ_EXTERNAL_STORAGE to Pair(false , false)
        )
        val testRegistry = object : ActivityResultRegistry() {
            override fun <I, O> onLaunch(
                requestCode: Int,
                contract: ActivityResultContract<I, O>,
                input: I,
                options: ActivityOptionsCompat?
            ) {
                dispatchResult(requestCode, expectedResult.mapValues { false })
            }
        }
        val mockRoot = mock(
            RootActivity::class.java,
            withSettings().defaultAnswer(RETURNS_DEEP_STUBS)
        )
//        val args = argumentCaptor<Map<String, Pair<Boolean, Boolean>>>()

        with(launchFragment(themeResId = R.style.AppTheme) {
            ProfileFragment(mockRoot, testRegistry,{ MockViewModelFactory(it)} )
        }) {
            onFragment { fragment ->

                // Trigger the ActivityResultLauncher
                fragment.viewModel.requestPermissions(expectedResult.keys.toList())
                // Verify the result is set

                verify(fragment.viewModel).handlePermission(capture(argCaptor))
                assertEquals(expectedResult, argCaptor.value)
                verify(fragment.viewModel, never()).executePendingAction()
                verify(fragment.viewModel).executeOpenSettings()

            }
        }

    }


    @Test
    fun take_result_from_camera() {

        val testRegistry = object : ActivityResultRegistry() {
            override fun <I, O> onLaunch(
                requestCode: Int,
                contract: ActivityResultContract<I, O>,
                input: I,
                options: ActivityOptionsCompat?
            ) {
                dispatchResult(requestCode, true)
            }
        }
        val mockRoot = mock(
            RootActivity::class.java,
            withSettings().defaultAnswer(RETURNS_DEEP_STUBS)
        )


        with(launchFragment(themeResId = R.style.AppTheme) {
            ProfileFragment(mockRoot, testRegistry,{ MockViewModelFactory(it)} )
        }) {
            onFragment { fragment ->
                // Trigger the ActivityResultLauncher
                val pendingAction = PendingAction.CameraAction(fragment.prepareTempUri())
                fragment.viewModel.updateState { it.copy(pendingAction = pendingAction) }
                fragment.viewModel.startForResult(pendingAction)
                // Verify the result is set
                verify(fragment.viewModel, atLeastOnce()).handleUploadPhoto(any())
            }
        }
    }

    @Test
    fun take_no_result_from_camera() {

        val testRegistry = object : ActivityResultRegistry() {
            override fun <I, O> onLaunch(
                requestCode: Int,
                contract: ActivityResultContract<I, O>,
                input: I,
                options: ActivityOptionsCompat?
            ) {
                dispatchResult(requestCode, false)
            }
        }
        val mockRoot = mock(
            RootActivity::class.java,
            withSettings().defaultAnswer(RETURNS_DEEP_STUBS)
        )


        with(launchFragment(themeResId = R.style.AppTheme) {
            ProfileFragment(mockRoot, testRegistry,{ MockViewModelFactory(it)} )
        }) {
            onFragment { fragment ->
                // Trigger the ActivityResultLauncher
                val pendingAction = PendingAction.CameraAction(fragment.prepareTempUri())
                fragment.viewModel.updateState { it.copy(pendingAction = pendingAction) }
                fragment.viewModel.startForResult(pendingAction)
                // Verify the result is set
                verify(fragment.viewModel, never()).handleUploadPhoto(any())
            }
        }
    }

    @Test
    fun take_result_from_gallery() {
        val expectedUri = Uri.parse("android.resource://ru.skillbranch.skillarticles/" + R.mipmap.ic_launcher)
        val testRegistry = object : ActivityResultRegistry() {
            override fun <I, O> onLaunch(
                requestCode: Int,
                contract: ActivityResultContract<I, O>,
                input: I,
                options: ActivityOptionsCompat?
            ) {
                dispatchResult(requestCode, expectedUri)
            }
        }
        val mockRoot = mock(
            RootActivity::class.java,
            withSettings().defaultAnswer(RETURNS_DEEP_STUBS)
        )

        with(launchFragment(themeResId = R.style.AppTheme) {
            ProfileFragment(mockRoot, testRegistry, { MockViewModelFactory(it)})
        }) {
            onFragment { fragment ->
                // Trigger the ActivityResultLauncher
                val pendingAction = PendingAction.GalleryAction("image/jpeg")
                fragment.viewModel.updateState { it.copy(pendingAction = pendingAction) }
                fragment.viewModel.startForResult(pendingAction)
                // Verify the result is set
                verify(fragment.viewModel, atLeastOnce()).handleUploadPhoto(any())

            }
        }
    }

    @Test
    fun take_no_result_from_gallery() {
        val expectedUri = Uri.parse("android.resource://ru.skillbranch.skillarticles/" + R.mipmap.ic_launcher)
        val testRegistry = object : ActivityResultRegistry() {
            override fun <I, O> onLaunch(
                requestCode: Int,
                contract: ActivityResultContract<I, O>,
                input: I,
                options: ActivityOptionsCompat?
            ) {
                dispatchResult(requestCode, null)
            }
        }
        val mockRoot = mock(
            RootActivity::class.java,
            withSettings().defaultAnswer(RETURNS_DEEP_STUBS)
        )

        with(launchFragment(themeResId = R.style.AppTheme) {
            ProfileFragment(mockRoot, testRegistry, { MockViewModelFactory(it)})
        }) {
            onFragment { fragment ->
                // Trigger the ActivityResultLauncher
                val pendingAction = PendingAction.GalleryAction("image/jpeg")
                fragment.viewModel.updateState { it.copy(pendingAction = pendingAction) }
                fragment.viewModel.startForResult(pendingAction)
                // Verify the result is set
                verify(fragment.viewModel, never()).handleUploadPhoto(any())

            }
        }
    }

    @Test
    fun take_result_from_edit_photo() {
        val expectedUri = Uri.parse("android.resource://ru.skillbranch.skillarticles/" + R.mipmap.ic_launcher)
        val testRegistry = object : ActivityResultRegistry() {
            override fun <I, O> onLaunch(
                requestCode: Int,
                contract: ActivityResultContract<I, O>,
                input: I,
                options: ActivityOptionsCompat?
            ) {
                dispatchResult(requestCode, expectedUri)
            }
        }
        val mockRoot = mock(
            RootActivity::class.java,
            withSettings().defaultAnswer(RETURNS_DEEP_STUBS)
        )

        with(launchFragment(themeResId = R.style.AppTheme) {
            ProfileFragment(mockRoot, testRegistry,{ MockViewModelFactory(it)})
        }) {
            onFragment { fragment ->
                // Trigger the ActivityResultLauncher
                val temp = fragment.prepareTempUri()
                val pendingAction = PendingAction.EditAction(expectedUri to temp)
                fragment.viewModel.updateState { it.copy(pendingAction = pendingAction) }
                fragment.viewModel.startForResult(pendingAction)
                // Verify the result is set
                verify(fragment.viewModel, atLeastOnce()).handleUploadPhoto(any())

            }
        }
    }

    @Test
    fun take_no_result_from_edit_photo() {
        val expectedUri = Uri.parse("android.resource://ru.skillbranch.skillarticles/" + R.mipmap.ic_launcher)
        val testRegistry = object : ActivityResultRegistry() {
            override fun <I, O> onLaunch(
                requestCode: Int,
                contract: ActivityResultContract<I, O>,
                input: I,
                options: ActivityOptionsCompat?
            ) {
                dispatchResult(requestCode, null)
            }
        }
        val mockRoot = mock(
            RootActivity::class.java,
            withSettings().defaultAnswer(RETURNS_DEEP_STUBS)
        )


        with(launchFragment(themeResId = R.style.AppTheme) {
            ProfileFragment(mockRoot, testRegistry,{ MockViewModelFactory(it)})
        }) {
            onFragment { fragment ->
                // Trigger the ActivityResultLauncher
                val temp = fragment.prepareTempUri()
                val pendingAction = PendingAction.EditAction(expectedUri to temp)
                fragment.viewModel.updateState { it.copy(pendingAction = pendingAction) }
                fragment.viewModel.startForResult(pendingAction)
                // Verify the result is set
                verify(fragment.viewModel, never()).handleUploadPhoto(any())

            }
        }
    }
}

class MockViewModelFactory(owner: SavedStateRegistryOwner) : AbstractSavedStateViewModelFactory(owner, null) {
    override fun <T : ViewModel?> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ): T {
        return spy(ProfileViewModel(handle)) as T
    }
}

inline fun <reified T : Any> argumentCaptor(): ArgumentCaptor<T> =
    ArgumentCaptor.forClass(T::class.java)

fun <T> capture(argumentCaptor: ArgumentCaptor<T>): T = argumentCaptor.capture()

private fun <T> any(): T {
    Mockito.any<T>()
    return uninitialized()
}

private fun <T> uninitialized(): T = null as T

