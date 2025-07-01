package com.example.eduquizz.features.quizzGame.screens

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.eduquizz.features.quizzGame.model.QuestionItem
import com.example.eduquizz.features.quizzGame.viewmodel.QuestionViewModel
import kotlinx.coroutines.delay
import com.example.eduquizz.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.eduquizz.navigation.Routes
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.eduquizz.DataSave.DataViewModel
import com.example.eduquizz.DataSave.UserPreferencesManager
import kotlin.random.Random
@SuppressLint("SuspiciousIndentation")
@Composable
fun MainView(currentLevel:String,name: String, modifier: Modifier = Modifier,navController: NavController,questionViewModel: QuestionViewModel
,dataviewModel: DataViewModel = hiltViewModel()) {
    LaunchedEffect(key1 = true) {
        questionViewModel.Init(dataviewModel,currentLevel)
        //questionViewModel.getAllQuestions("English/QuizGame/$currentLevel")
    }
/*
    val count = remember { mutableStateOf(0) }
    val score = remember { mutableStateOf(0) }
    val choiceSelected = remember { mutableStateOf("") }
    val resetTimeTrigger = remember { mutableStateOf(0) }
    val questions = questionViewModel.data.value.data
    val usedQuestions = remember { mutableStateListOf<QuestionItem>() }
    val reserveQuestions = remember { mutableStateListOf<QuestionItem>() }
    val usedHelperThisQuestion = remember { mutableStateOf(false) }
    val showExpertDialog = remember { mutableStateOf(false) }
    val choiceAttempts = remember { mutableStateOf(0) }
    val gold by dataviewModel.gold.observeAsState(-1)
*/

    val count = questionViewModel.count
    val score = questionViewModel.score
    val choiceSelected = questionViewModel.choiceSelected
    val resetTimeTrigger = questionViewModel.resetTimeTrigger
    val questions = questionViewModel.data.value.data
    val usedQuestions = questionViewModel.usedQuestions
    val reserveQuestions = questionViewModel.reserveQuestions
    val usedHelperThisQuestion = questionViewModel.usedHelperThisQuestion
    val showExpertDialog = questionViewModel.showExpertDialog
    val choiceAttempts = questionViewModel.choiceAttempts
    val gold by dataviewModel.gold.observeAsState(-1)
    LaunchedEffect(questions) {
        if (questions != null && usedQuestions.isEmpty() && questions.size >= 20) {
            usedQuestions.clear()
            reserveQuestions.clear()
            val shuffledQuestions = questions.shuffled()
            usedQuestions.addAll(shuffledQuestions.take(10))
            reserveQuestions.addAll(shuffledQuestions.drop(10))
        }
    }
    val hiddenChoices = questionViewModel.hiddenChoices
    val helperCounts = questionViewModel.helperCounts
    val showResultDialog = questionViewModel.showResultDialog
    val expertAnswer = questionViewModel.expertAnswer
    val twoTimeChoice = questionViewModel.twoTimeChoice
    val coins = questionViewModel.coins
// Chỉ khởi tạo coins từ gold 1 lần duy nhất
    LaunchedEffect(gold) {
        if (gold >-1 && coins.value == -1) {
            coins.value = gold
        }
    }
/*    fun spendCoins(amount: Int) {
        coins.value = (coins.value ?: 0) - amount
        dataviewModel.updateGold(coins.value ?: 0) // <-- chỉ update khi cần
    }*/
    Scaffold(
        bottomBar = {
            BottomHelperBar(
                usedHelperThisQuestion = usedHelperThisQuestion.value, // truyền vào đây
                coins = coins.value,
                helperCounts = helperCounts,
                onHelperClick = { index ->
                    if (usedHelperThisQuestion.value) return@BottomHelperBar
                        questionViewModel.ProcessHelperBar(index)
 /*                   if(index == 0&&(helperCounts[index].second <= coins.value)&&choiceSelected.value.isEmpty() ){
                        val currentQuestion = usedQuestions?.get(count.value)
                        if(currentQuestion!=null){
                            val wrongAnswers = currentQuestion.choices.filter { it != currentQuestion.answer }
                            hiddenChoices.clear()
                            hiddenChoices.addAll(wrongAnswers.shuffled().take(2))
                            //coins.value -= helperCounts[index].second
                            spendCoins(helperCounts[index].second)
                            usedHelperThisQuestion.value = true
                        }
                    }
                    else if (index == 1 && helperCounts[index].second <= coins.value && choiceSelected.value.isEmpty()) {
                        if (count.value < usedQuestions.size && reserveQuestions.isNotEmpty()) {
                            val newQuestion = reserveQuestions.removeAt(0)

                            // Thay thế câu hỏi hiện tại
                            usedQuestions.removeAt(count.value)
                            usedQuestions.add(count.value, newQuestion)

                            // Reset trạng thái
                            //coins.value -= helperCounts[index].second
                            spendCoins(helperCounts[index].second)
                            hiddenChoices.clear()
                            choiceSelected.value = ""
                            resetTimeTrigger.value++ // cũng có thể reset timer nếu cần
                            usedHelperThisQuestion.value = true
                        }
                    }
                    else if(index==2&&helperCounts[index].second <= coins.value&& choiceSelected.value.isEmpty())
                    {
                        showExpertDialog.value = true
                        //coins.value -= helperCounts[index].second
                        spendCoins(helperCounts[index].second)
                        val currentQuestion = usedQuestions[count.value]
                        val correctAnswer = currentQuestion.answer
                        val wrongAnswers = currentQuestion.choices.filter { it != correctAnswer }
                        // Lấy kí hiệu đáp án (A, B, C, D)
                        fun getLetter(index: Int): String {
                            return when(index) {
                                0 -> "A"
                                1 -> "B"
                                2 -> "C"
                                3 -> "D"
                                else -> "?"
                            }
                        }
                        expertAnswer.value = if (Random.nextFloat() < 0.9f) {
                            getLetter(currentQuestion.choices.indexOf(correctAnswer))
                        } else {
                            getLetter(currentQuestion.choices.indexOf(wrongAnswers.random()))
                        }
                        showExpertDialog.value = true
                        usedHelperThisQuestion.value = true
                    }else if(index == 3&&helperCounts[index].second <= coins.value&& choiceSelected.value.isEmpty()){
                        twoTimeChoice.value = true
                        //coins.value -= helperCounts[index].second
                        spendCoins(helperCounts[index].second)
                        usedHelperThisQuestion.value = true
                    }*/
                },
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(bottom = 8.dp), // tránh đụng vào BottomHelperBar
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
          var context = LocalContext.current
            TimerProgressBar(
                resetTrigger = resetTimeTrigger.value,
                isAnswered = choiceSelected.value.isNotEmpty(),
                onTimeOut = {
                    if(choiceSelected.value.isEmpty()){
                        choiceSelected.value = "timeout"
                        usedHelperThisQuestion.value = true
                        Toast.makeText(context, "Đã hết thời gian !!!", Toast.LENGTH_SHORT).show()
                    }
                }
            )

            if (questionViewModel.data.value.loading==false && usedQuestions.isNotEmpty() && count.value < usedQuestions.size) {
                Spacer(modifier = Modifier.height(20.dp))
                ScoreScreen(
                    count = count,
                    //totalQuestion = questionViewModel.getTotalQuestionCount(),
                    totalQuestion = usedQuestions.size,
                    score = score
                )
                QuestionScreen(usedQuestions[count.value])
                ChoiceScreen(
                    twoTimeChoice = twoTimeChoice.value,
                    count = count,
                    questionItem = usedQuestions[count.value],
                    choiceSelected = choiceSelected,
                    score = score,
                    choiceAttempts = choiceAttempts,
                    hiddenChoice = hiddenChoices,
                    totalQuestion = usedQuestions.size,
                    onNext = {
                        if (count.value >= usedQuestions.lastIndex) {
                            //showResultDialog.value = true
                            val temp :Int = usedQuestions.size
                            val score_temp = score.value
                            navController.navigate("result/$score_temp/$temp")
                        } else {
                            count.value++
                            choiceSelected.value = ""
                            resetTimeTrigger.value++
                            hiddenChoices.clear()
                            usedHelperThisQuestion.value = false
                            twoTimeChoice.value = false
                            choiceAttempts.value = 0 // RESET CHỌN LẠI
                        }
                    }
                )
            }
        }
        if (showExpertDialog.value) {
            AlertDialog(
             //   modifier = Modifier.height(400.dp).width(200.dp),
                onDismissRequest = { showExpertDialog.value = false },
                title = { Text("Ý kiến chuyên gia") },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(
                            painter = painterResource(id = R.drawable.chuyengiabig), // ảnh trong drawable
                            contentDescription = "Expert",
                            modifier = Modifier
                                .size(150.dp)
                                .clip(RoundedCornerShape(16.dp)) // bo góc ảnh mềm mại
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Đáp án của tổ tư vấn là ${expertAnswer.value} với tỉ lệ 80% là đúng.")
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showExpertDialog.value = false }) {
                        Text("Đóng")
                    }
                }
            )
        }

        if (showResultDialog.value) {
            AlertDialog(
                onDismissRequest = {},
                title = { Text("Quiz Complete!") },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(
                            painter = painterResource( id =if(score.value>=10){ R.drawable.congratgif}else{
                                R.drawable.betterluck
                            }), // ảnh trong drawable
                            contentDescription = "Congrat",
                            modifier = Modifier
                                .size(150.dp)
                                .clip(RoundedCornerShape(16.dp)) // bo góc ảnh mềm mại
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        if(score.value<10){
                            Text("Try your best later")
                        }
                        Text("Your score is ${score.value}/${usedQuestions?.size}")
                        Text("You earned ${score.value * 10} coins!")
                    }
                },

                confirmButton = {
                    Button(
                        onClick = {
                            // Cập nhật xu nếu muốn
                            coins.value += score.value * 10
                            showResultDialog.value = false
                            navController.navigate(Routes.INTRO) // ← Replace with your destination
                        }
                    ) {
                        Text("OK")
                    }
                }
            )
        }
    }
}


@Composable
fun ScoreScreen(count: MutableState<Int>, totalQuestion: Int, score: MutableState<Int>) {
    Row(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Question ${count.value + 1}",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF3949AB)
            )
            Text(
                text = "/$totalQuestion",
                fontSize = 20.sp,
                color = Color.Gray,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Score: ${score.value}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF2E7D32),
                modifier = Modifier.padding(end = 12.dp)
            )
           // TimerCount(timeTotal = 30, timeWarning = 10)
        }
    }
}
@Composable
fun BottomHelperBar(
    modifier: Modifier = Modifier,
    coins: Int,
    helperCounts: List<Pair<Int, Int>>,
    usedHelperThisQuestion: Boolean, // thêm dòng này
    onHelperClick: (index: Int) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
           // .background(Color.White)
            .padding(2.dp)
    ) {
        // Coins
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.coinimg),
                contentDescription = "Coin",
                //tint = Color.Unspecified,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = coins.toString(),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Helper items
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            helperCounts.forEachIndexed { index, (iconId, count) ->
                val helperAlpha = if (usedHelperThisQuestion) 0.3f else 1f // mờ đi nếu đã dùng
                val helperEnabled = !usedHelperThisQuestion // vô hiệu hóa click nếu đã dùng
                Box(
                    modifier = Modifier
                        .clickable { onHelperClick(index) }
                        .padding(4.dp)
                        .alpha(helperAlpha),

                    contentAlignment = Alignment.BottomEnd
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(25.dp))
                            .background(Color.White)
                            .border(2.dp, Color.LightGray, RoundedCornerShape(25.dp))
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = iconId),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Box(
                        modifier = Modifier
                            .offset(x = 8.dp, y = 8.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Row{
                            Text(
                                text = "$count",
                                fontSize = 15.sp,
                                color = Color.Black,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(1.dp))
                            Image(
                                painter = painterResource(id = R.drawable.coinimg),
                                contentDescription = null,
                                modifier = Modifier.size(23.dp)
                            )
                        }

                    }
                }
            }
        }
    }
}
@Composable
fun TimerProgressBar(
    totalTime: Int = 30,
    warningTime: Int = 10,
    resetTrigger: Int = 0,
    isAnswered: Boolean = false,
    onTimeOut: () -> Unit = {},
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(6.dp)
        .padding(horizontal = 0.dp)
) {
    var timeLeft by remember(resetTrigger) { mutableStateOf(totalTime) }
    var progress by remember(resetTrigger) { mutableStateOf(1f) }

    LaunchedEffect(resetTrigger, isAnswered) {
        while (timeLeft > 0 && !isAnswered) {
            delay(1000L)
            timeLeft--
            progress = timeLeft.toFloat() / totalTime.toFloat()
        }
        if(timeLeft<=0&&!isAnswered){
            onTimeOut()
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(100.dp))
            .background(Color.LightGray)
    ) {
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier.fillMaxSize(),
            color = if (timeLeft <= warningTime) Color.Red else Color(0xFF3F51B5),
            trackColor = Color.Transparent
        )
    }
}





@Composable
fun QuestionScreen(questionItem: QuestionItem) {
    val scrollState = rememberScrollState()
    val imageUrl = questionItem.image ?: ""

    Column(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (imageUrl.isNotBlank()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Question Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Fit
//                placeholder = painterResource(id = R.drawable.placeholder), // ảnh tạm
//                error = painterResource(id = R.drawable.error_image)        // ảnh lỗi
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        // ✅ Hiển thị câu hỏi dạng text
        Text(
            text = questionItem.question,
            color = Color(0xFF1A237E),
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ChoiceScreen(
    twoTimeChoice: Boolean = false,
    count: MutableState<Int>,
    totalQuestion:Int,
    choiceAttempts: MutableState<Int>,
    questionItem: QuestionItem,
    choiceSelected: MutableState<String>,
    score: MutableState<Int>,
    hiddenChoice: List<String>,
    context: Context = LocalContext.current,
    onNext: () -> Unit
) {
    val wrongChoices = remember { mutableStateListOf<String>() }
    val forceUpdate = remember { mutableStateOf(false) } // để ép recompose nếu cần

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(4.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            questionItem.choices.forEachIndexed { index, choice ->
                val isSelected = choice == choiceSelected.value
                val isCorrect = if (choiceSelected.value.isNotEmpty()) {
                    choice == questionItem.answer
                } else null
                val check_50_50 = hiddenChoice.contains(choice)
                val isWrong = wrongChoices.contains(choice)

                ChoiceButton(
                    twoTimeChoice = twoTimeChoice,
                    content = choice,
                    isSelected = isSelected,
                    isCorrectAnswer = isCorrect,
                    isDisabled = check_50_50 || isWrong,
                    showAnswer = choiceSelected.value.isNotEmpty(),
                    stt = index + 1,
                    onClick = {
                        if (choiceSelected.value.isEmpty()) {
                            if (choice == questionItem.answer) {
                                choiceSelected.value = choice
                                score.value++
                            } else if (twoTimeChoice && choiceAttempts.value == 0) {
                                // Cho chọn lại
                                wrongChoices.add(choice)
                                choiceAttempts.value++
                                Toast.makeText(
                                    context,
                                    "Đáp án không chính xác !! Bạn được chọn lại một lần nữa!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                choiceSelected.value = choice
                                wrongChoices.add(choice)
                            }
                            forceUpdate.value = !forceUpdate.value
                        } else if (choiceSelected.value == "timeout") {
                            Toast.makeText(context, "Bạn đã hết thời gian !!!", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
        val color_btn = when{
            count.value == (totalQuestion-1) ->  ButtonDefaults.buttonColors(containerColor = Color(
                0xFF03A9F4
            )
            )
            else ->  ButtonDefaults.buttonColors(containerColor = Color(0xFFEC407A))
        }
        Button(
            onClick = {
                onNext() },
            shape = RoundedCornerShape(20.dp),
            colors = color_btn,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 0.dp)
        ) {
            val text_NextQuestion = when {
                count.value == (totalQuestion-1) -> "Finish Test"
                else -> "Next"
            }

            Text(
                text = text_NextQuestion,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ChoiceButton(
    stt: Int,
    twoTimeChoice: Boolean,
    content: String,
    isSelected: Boolean,
    isCorrectAnswer: Boolean?,
    showAnswer: Boolean,
    isDisabled: Boolean,
    onClick: () -> Unit
) {
     var backgroundColor = when {
        //isDisabled -> Color(0xFFA9A7A7)
        isSelected && isCorrectAnswer == true -> Color(0xFF4CAF50)
        isSelected && isCorrectAnswer == false -> Color(0xFF9F3E35)
        showAnswer && isCorrectAnswer == true -> Color(0xFF4CAF50)

        else -> Color(0xFFF5F5F5)
    }

    val textColor = when {
        isDisabled -> Color(0xFFFFFFFF)
        isSelected || (showAnswer && isCorrectAnswer == true) -> Color.White
        else -> Color.Black
    }

    val borderModifier = if (isSelected) {
        Modifier.border(
            width = 2.dp,
            color = Color(0xFF6200EE),
            shape = RoundedCornerShape(12.dp)
        )
    } else Modifier

    Button(
        onClick = onClick,
        enabled = (!isDisabled),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 16.dp)
            .then(borderModifier)
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),

        colors = ButtonDefaults.buttonColors(
                containerColor = backgroundColor,
                contentColor = textColor,
            disabledContainerColor = Color(0xFF9F3E35),  // Dùng cùng màu nền khi disable
            disabledContentColor = textColor           // Dùng cùng màu chữ khi disable
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        )
    ) {
        val prefix = when (stt) {
            1 -> "A."
            2 -> "B."
            3 -> "C."
            else -> "D."
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text(
                text = prefix,
                color = textColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(
                text = content,
                color = textColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Start
            )
        }

    }
}
