package pl.wsei.pam.lab03

import android.content.res.TypedArray
import android.view.Gravity
import android.view.View
import android.widget.GridLayout
import android.widget.ImageButton
import pl.wsei.pam.lab01.R
import java.util.Stack


class MemoryBoardView(
    private val gridLayout: GridLayout,
    private val cols: Int,
    private val rows: Int
) {
    private val tiles: MutableMap<String, Tile> = mutableMapOf()
    private val icons: List<Int> = listOf(
        R.drawable.baseline_rocket_launch_24,
        R.drawable.baseline_photo_24 ,
        R.drawable.baseline_pest_control_rodent_24 ,
        R.drawable.baseline_public_24 ,
        R.drawable.baseline_sledding_24 ,
        R.drawable.baseline_time_to_leave_24 ,
        R.drawable.baseline_wb_sunny_24 ,
        R.drawable.baseline_wind_power_24 ,
        R.drawable.baseline_whatshot_24 ,
        R.drawable.baseline_subway_24 ,
        R.drawable.baseline_star_rate_24 ,
        R.drawable.baseline_star_purple500_24 ,
        R.drawable.baseline_sports_tennis_24 ,
        R.drawable.baseline_sports_soccer_24 ,
        R.drawable.baseline_sports_esports_24 ,
        R.drawable.baseline_sports_football_24 ,
        R.drawable.baseline_sports_basketball_24 ,
        R.drawable.baseline_satellite_alt_24
    )
    private var state: List<Int> = mutableListOf<Int>();
    private val deckResource: Int = R.drawable.baseline_remove_red_eye_24
    init {
        val shuffledIcons: MutableList<Int> = mutableListOf<Int>().also {
            it.addAll(icons.subList(0, cols * rows / 2))
            it.addAll(icons.subList(0, cols * rows / 2))
            it.shuffle()
        }

        // tu umieść kod pętli tworzący wszystkie karty, który jest obecnie
        // w aktywności Lab03Activity

        var iconIndex = 0;

        for (i in 0 ..< rows) {
            for (j in 0 ..< cols) {
                val button = ImageButton(gridLayout.context).also {
                    it.tag = "$i,$j"
                    it.setImageResource(deckResource)
                    val layoutParams = GridLayout.LayoutParams().apply {
                        width = GridLayout.LayoutParams.WRAP_CONTENT
                        height = GridLayout.LayoutParams.WRAP_CONTENT
                        setGravity(Gravity.CENTER)
                        columnSpec = GridLayout.spec(j, 1f)
                        rowSpec = GridLayout.spec(i, 1f)
                    }
                    it.layoutParams = layoutParams
                    gridLayout.addView(it)
                }

                val resourceImage = shuffledIcons[iconIndex++]
                addTile(button, resourceImage)
            }
        }
    }

    private var onGameChangeStateListener: (MemoryGameEvent) -> Unit = { (e) -> }
    private val matchedPair: Stack<Tile> = Stack()
    private val logic: MemoryGameLogic = MemoryGameLogic(cols * rows / 2)

    private fun onClickTile(v: View) {
        val tile = tiles[v.tag]
        matchedPair.push(tile)
        val matchResult = logic.process {
            tile?.tileResource?:-1
        }
        onGameChangeStateListener(MemoryGameEvent(matchedPair.toList(), matchResult))
        if (matchResult != GameStates.Matching) {
            matchedPair.clear()
        }
    }

    fun setOnGameChangeListener(listener: (event: MemoryGameEvent) -> Unit) {
        onGameChangeStateListener = listener
    }

    private fun addTile(button: ImageButton, resourceImage: Int) {
        button.setOnClickListener(::onClickTile)
        val tile = Tile(button, resourceImage, deckResource)
        tiles[button.tag.toString()] = tile
    }

    fun getState(): List<TileState> {
        val output: MutableList<TileState> = mutableListOf();

         tiles.forEach {
             var newTileState = TileState(it.value.tileResource, it.value.revealed, it.key);
             output.add(newTileState);
         }

        return output;
    }

    fun setState(imagesArray: IntArray, revealedArray: BooleanArray, tagsArray: Array<String>) {
        var index: Int = 0;
        tiles.forEach{
            it.value.tileResource = imagesArray[index];
            it.value.revealed = revealedArray[index];

            index++;
        }
    }
}

data class TileState(
    val id: Int, //obrazki
    val revealed: Boolean,
    val tag: String
){}
