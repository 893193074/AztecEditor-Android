package org.wordpress.aztec

import android.app.Activity
import android.text.TextUtils
import android.view.KeyEvent
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import android.widget.EditText


/**
 * Testing ordered and unordered lists.
 *
 * This test uses ParameterizedRobolectricTestRunner and runs twice - for ol and ul tags.
 */
@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(constants = BuildConfig::class, manifest = "src/main/AndroidManifest.xml", sdk = intArrayOf(23))
class ListTest(listTextFormat: TextFormat, listHtmlTag: String) {

    val listType = listTextFormat
    val listTag = listHtmlTag
    lateinit var editText: AztecText

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "Testing lists with {1} tag")
        fun data(): Collection<Array<Any>> {
            return listOf(
//                    arrayOf(TextFormat.FORMAT_ORDERED_LIST, "ol"),
                    arrayOf(TextFormat.FORMAT_UNORDERED_LIST, "ul")
            )
        }
    }


    /**
     * Initialize variables.
     */
    @Before
    fun init() {
        val activity = Robolectric.buildActivity(Activity::class.java).create().visible().get()
        editText = AztecText(activity)
        activity.setContentView(editText)
    }

    //enter text and then enable styling

    @Test
    @Throws(Exception::class)
    fun styleSingleItem() {
        safeAppend(editText, "first item")
        editText.toggleFormatting(listType)
        Assert.assertEquals("<$listTag><li>first item</li></$listTag>", editText.toHtml())
    }


    @Test
    @Throws(Exception::class)
    fun styleMultipleSelectedItems() {
        Assert.assertTrue(safeEmpty(editText))
        safeAppend(editText, "first item")
        safeAppend(editText, "\n")
        safeAppend(editText, "second item")
        safeAppend(editText, "\n")
        safeAppend(editText, "third item")
        editText.setSelection(0, editText.length())

        editText.toggleFormatting(listType)
        Assert.assertEquals("<$listTag><li>first item</li><li>second item</li><li>third item</li></$listTag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun stylePartiallySelectedMultipleItems() {
        Assert.assertTrue(safeEmpty(editText))
        safeAppend(editText, "first item")
        safeAppend(editText, "\n")
        safeAppend(editText, "second item")
        safeAppend(editText, "\n")
        safeAppend(editText, "third item")
        editText.setSelection(4, 15) //we partially selected first and second item

        editText.toggleFormatting(listType)
        Assert.assertEquals("<$listTag><li>first item</li><li>second item</li></$listTag>third item", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun styleSurroundedItem() {
        Assert.assertTrue(safeEmpty(editText))
        safeAppend(editText, "first item")
        safeAppend(editText, "\n")
        safeAppend(editText, "second item")
        safeAppend(editText, "\n")
        safeAppend(editText, "third item")
        editText.setSelection(14)

        editText.toggleFormatting(listType)
        Assert.assertEquals("first item<$listTag><li>second item</li></$listTag>third item", editText.toHtml())
    }


    //enable styling on empty line and enter text

    @Test
    @Throws(Exception::class)
    fun emptyList() {
        editText.toggleFormatting(listType)
        Assert.assertEquals("<$listTag><li></li></$listTag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun styleSingleEnteredItem() {
        editText.toggleFormatting(listType)
        safeAppend(editText, "first item")
        Assert.assertEquals("<$listTag><li>first item</li></$listTag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun styleMultipleEnteredItems() {
        editText.toggleFormatting(listType)
        safeAppend(editText, "first item")
        safeAppend(editText, "\n")
        safeAppend(editText, "second item")
        Assert.assertEquals("<$listTag><li>first item</li><li>second item</li></$listTag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun closingPopulatedList() {
        editText.toggleFormatting(listType)
        safeAppend(editText, "first item")
        safeAppend(editText, "\n")
        safeAppend(editText, "second item")

        safeAppend(editText, "\n")
        safeAppend(editText, "\n")
        safeAppend(editText, "not in the list")
        Assert.assertEquals("<$listTag><li>first item</li><li>second item</li></$listTag>not in the list", editText.toHtml())
    }


    @Test
    @Throws(Exception::class)
    fun closingEmptyList() {
        editText.toggleFormatting(listType)
        safeAppend(editText, "\n")
        Assert.assertEquals("", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun extendingListBySplittingItems() {
        editText.toggleFormatting(listType)
        safeAppend(editText, "firstitem")
        editText.text.insert(5, "\n")
        Assert.assertEquals("<$listTag><li>first</li><li>item</li></$listTag>", editText.toHtml())
    }


    @Test
    @Throws(Exception::class)
    fun bulletListSplitWithToolbar() {
        editText.fromHtml("<$listTag><li>first item</li><li>second item</li><li>third item</li></$listTag>")
        editText.setSelection(14)
        editText.toggleFormatting(listType)

        Assert.assertEquals("<$listTag><li>first item</li></$listTag>second item<$listTag><li>third item</li></$listTag>", editText.toHtml())
    }


    @Test
    @Throws(Exception::class)
    fun splitTwoListsWithNewline() {
        editText.fromHtml("<$listTag><li>List 1</li></$listTag><$listTag><li>List 2</li></$listTag>")
        val mark2 = editText.text.indexOf("List 2")
        editText.text.insert(mark2, "\n")
        Assert.assertEquals("<$listTag><li>List 1</li></$listTag><$listTag><li></li><li>List 2</li></$listTag>", editText.toHtml())

        val mark1 = editText.text.indexOf("List 1") + "List 1".length
        editText.text.insert(mark1, "\n")
        Assert.assertEquals("<$listTag><li>List 1</li><li></li></$listTag><$listTag><li></li><li>List 2</li></$listTag>", editText.toHtml())

        editText.text.insert(mark1 + 1, "\n")
        Assert.assertEquals("<$listTag><li>List 1</li></$listTag><br><$listTag><li></li><li>List 2</li></$listTag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun removeBulletListStyling() {
        editText.fromHtml("<$listTag><li>first item</li></$listTag>")
        editText.setSelection(1)
        editText.toggleFormatting(listType)

        Assert.assertEquals("first item", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun removeBulletListStylingForPartialSelection() {
        editText.fromHtml("<$listTag><li>first item</li></$listTag>")
        editText.setSelection(2, 4)
        editText.toggleFormatting(listType)

        Assert.assertEquals("first item", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun removeBulletListStylingForMultilinePartialSelection() {
        editText.toggleFormatting(listType)
        safeAppend(editText, "first item")
        safeAppend(editText, "\n")
        safeAppend(editText, "second item")
        val firstMark = editText.length() - 4
        safeAppend(editText, "\n")
        safeAppend(editText, "third item")
        safeAppend(editText, "\n")
        val secondMark = editText.length() - 4
        safeAppend(editText, "fourth item")
        safeAppend(editText, "\n")
        safeAppend(editText, "\n")
        safeAppend(editText, "not in list")

        editText.setSelection(firstMark, secondMark)
        editText.toggleFormatting(listType)

        Assert.assertEquals("<$listTag><li>first item</li></$listTag>second item<br>third item<$listTag><li>fourth item</li></$listTag>not in list", editText.toHtml())
    }


    @Test
    @Throws(Exception::class)
    fun emptyBulletSurroundedBytItems() {
        editText.toggleFormatting(listType)
        safeAppend(editText, "first item")
        safeAppend(editText, "\n")
        safeAppend(editText, "second item")
        safeAppend(editText, "\n")
        safeAppend(editText, "third item")

        val start = editText.text.indexOf("second item")
        val end = start + "second item".length

        editText.text.delete(start, end)

        Assert.assertEquals("<$listTag><li>first item</li><li></li><li>third item</li></$listTag>", editText.toHtml())
    }


    @Test
    @Throws(Exception::class)
    fun trailingEmptyBulletPoint() {
        editText.toggleFormatting(listType)
        safeAppend(editText, "first item")
        safeAppend(editText, "\n")
        safeAppend(editText, "second item")
        safeAppend(editText, "\n")
        safeAppend(editText, "third item")
        val mark = editText.length()
        safeAppend(editText, "\n")

        Assert.assertEquals("<$listTag><li>first item</li><li>second item</li><li>third item</li><li></li></$listTag>", editText.toHtml())
        safeAppend(editText, "\n")

        Assert.assertEquals("<$listTag><li>first item</li><li>second item</li><li>third item</li></$listTag>", editText.toHtml())

        safeAppend(editText, "not in list")
        editText.setSelection(mark)
        editText.text.insert(mark, "\n")
        Assert.assertEquals("<$listTag><li>first item</li><li>second item</li><li>third item</li><li></li></$listTag>not in list", editText.toHtml())
    }


    @Test
    @Throws(Exception::class)
    fun openListByAddingNewline() {
        editText.fromHtml("<$listTag><li>first item</li><li>second item</li></$listTag>not in list")
        editText.setSelection(editText.length())

        editText.text.insert(editText.text.indexOf("not in list") - 1, "\n")
        editText.text.insert(editText.text.indexOf("not in list") - 1, "third item")
        Assert.assertEquals("<$listTag><li>first item</li><li>second item</li><li>third item</li></$listTag>not in list", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun openListByAppendingTextToTheEnd() {
        editText.fromHtml("<$listTag><li>first item</li><li>second item</li></$listTag>not in list")
        editText.setSelection(editText.length())
        editText.text.insert(editText.text.indexOf("not in list") - 1, " (appended)")
        Assert.assertEquals("<$listTag><li>first item</li><li>second item (appended)</li></$listTag>not in list", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun openListByMovingOutsideTextInsideList() {
        editText.fromHtml("<$listTag><li>first item</li><li>second item</li></$listTag>not in list")
        editText.setSelection(editText.length())
        editText.text.delete(editText.text.indexOf("not in list") - 1, editText.text.indexOf("not in list"))
        Assert.assertEquals("<$listTag><li>first item</li><li>second itemnot in list</li></$listTag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun listRemainsClosedWhenLastCharacterIsDeleted() {
        editText.fromHtml("<$listTag><li>first item</li><li>second item</li></$listTag>not in list")
        editText.setSelection(editText.length())

        //delete last character from "second item"
        editText.text.delete(editText.text.indexOf("not in list") - 2, editText.text.indexOf("not in list") - 1)
        Assert.assertEquals("<$listTag><li>first item</li><li>second ite</li></$listTag>not in list", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun openingAndReopeningOfList() {
        editText.fromHtml("<$listTag><li>first item</li><li>second item</li></$listTag>")
        editText.setSelection(editText.length())

        safeAppend(editText, "\n")
        safeAppend(editText, "third item")
        Assert.assertEquals("<$listTag><li>first item</li><li>second item</li><li>third item</li></$listTag>", editText.toHtml())
        safeAppend(editText, "\n")
        safeAppend(editText, "\n")
        safeAppend(editText, "not in the list")
        val mark = editText.text.indexOf("not in the list")
        Assert.assertEquals("<$listTag><li>first item</li><li>second item</li><li>third item</li></$listTag>not in the list", editText.toHtml())
        safeAppend(editText, "\n")
        safeAppend(editText, "foo")
        Assert.assertEquals("<$listTag><li>first item</li><li>second item</li><li>third item</li></$listTag>not in the list<br>foo", editText.toHtml())

        //reopen list
        editText.text.delete(mark - 1, mark) // delete the newline before the not-in-the-list text
        Assert.assertEquals("<$listTag><li>first item</li><li>second item</li><li>third itemnot in the list</li></$listTag>foo", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun closeList() {
        editText.fromHtml("<$listTag><li>first item</li><li>second item</li></$listTag>")
        editText.setSelection(editText.length())

        safeAppend(editText, "\n")
        safeAppend(editText, "\n")
        val mark = editText.length() - 1

        editText.text.delete(mark, mark + 1)
        safeAppend(editText, "not in the list")
        Assert.assertEquals("<$listTag><li>first item</li><li>second item</li></$listTag>not in the list", editText.toHtml())
    }


    @Test
    @Throws(Exception::class)
    fun handleListReopeningAfterLastElementDeletion() {
        editText.fromHtml("<$listTag><li>first item</li><li>second item</li><li>third item</li></$listTag>")
        editText.setSelection(editText.length())

        editText.text.delete(editText.text.indexOf("third item", 0), editText.length())

        safeAppend(editText, "\n")
        safeAppend(editText, "not in the list")
        Assert.assertEquals("<$listTag><li>first item</li><li>second item</li></$listTag>not in the list", editText.toHtml())

        editText.text.insert(editText.text.indexOf("not in the list") - 1, " addition")
        Assert.assertEquals("<$listTag><li>first item</li><li>second item addition</li></$listTag>not in the list", editText.toHtml())

        editText.text.insert(editText.text.indexOf("not in the list") - 1, "\n")
        editText.text.insert(editText.text.indexOf("not in the list") - 1, "third item")
        Assert.assertEquals("<$listTag><li>first item</li><li>second item addition</li><li>third item</li></$listTag>not in the list", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun additionToClosedList() {
        editText.toggleFormatting(listType)
        safeAppend(editText, "first item")
        safeAppend(editText, "\n")
        safeAppend(editText, "second item")

        val mark = editText.length()

        safeAppend(editText, "\n")
        safeAppend(editText, "\n")
        safeAppend(editText, "not in the list")
        Assert.assertEquals("<$listTag><li>first item</li><li>second item</li></$listTag>not in the list", editText.toHtml())

        editText.text.insert(mark, " (addition)")

        Assert.assertEquals("<$listTag><li>first item</li><li>second item (addition)</li></$listTag>not in the list", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun toggleListType() {

        val oppositeTextFormat = if (listType == TextFormat.FORMAT_ORDERED_LIST)
            TextFormat.FORMAT_UNORDERED_LIST else TextFormat.FORMAT_ORDERED_LIST

        val oppositeTag = if (listTag == "ol") "ul" else "ol"

        editText.fromHtml("<$listTag><li>first item</li><li>second item</li><li>third item</li></$listTag>")
        editText.setSelection(editText.length())

        editText.toggleFormatting(oppositeTextFormat)

        Assert.assertEquals("<$oppositeTag><li>first item</li><li>second item</li><li>third item</li></$oppositeTag>", editText.toHtml())

        editText.toggleFormatting(listType)
        Assert.assertEquals("<$listTag><li>first item</li><li>second item</li><li>third item</li></$listTag>", editText.toHtml())
    }


    @Test
    @Throws(Exception::class)
    fun addItemToListFromBottom() {
        editText.toggleFormatting(listType)
        safeAppend(editText, "first item")
        safeAppend(editText, "\n")
        safeAppend(editText, "second item")
        safeAppend(editText, "\n")
        safeAppend(editText, "\n")
        editText.text.delete(editText.length() - 1, editText.length())
        safeAppend(editText, "third item")
        editText.setSelection(editText.length())

        editText.toggleFormatting(listType)

        Assert.assertEquals("<$listTag><li>first item</li><li>second item</li><li>third item</li></$listTag>", editText.toHtml())
    }


    @Test
    @Throws(Exception::class)
    fun addItemToListFromTop() {
        safeAppend(editText, "first item")
        safeAppend(editText, "\n")
        safeAppend(editText, "second item")
        editText.setSelection(editText.length())
        editText.toggleFormatting(listType)
        safeAppend(editText, "\n")
        safeAppend(editText, "third item")

        editText.setSelection(0)

        editText.toggleFormatting(listType)

        Assert.assertEquals("<$listTag><li>first item</li><li>second item</li><li>third item</li></$listTag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun addItemToListFromInside() {
        editText.toggleFormatting(listType)
        safeAppend(editText, "first item")
        safeAppend(editText, "\n")
        safeAppend(editText, "\n")
        editText.text.delete(editText.length() - 1, editText.length())
        safeAppend(editText, "second item")
        safeAppend(editText, "\n")
        safeAppend(editText, "third item")
        editText.setSelection(editText.length())
        editText.toggleFormatting(listType)

        Assert.assertEquals("<$listTag><li>first item</li></$listTag>second item<$listTag><li>third item</li></$listTag>", editText.toHtml())
        editText.setSelection(15)
        editText.toggleFormatting(listType)

        Assert.assertEquals("<$listTag><li>first item</li><li>second item</li><li>third item</li></$listTag>", editText.toHtml())
    }


    @Test
    @Throws(Exception::class)
    fun appendToListFromTopAtFirstLine() {
        editText.toggleFormatting(listType)
        safeAppend(editText, "first item")
        safeAppend(editText, "\n")
        safeAppend(editText, "second item")
        editText.setSelection(0)
        editText.text.insert(0,"addition ")

        Assert.assertEquals("<$listTag><li>addition first item</li><li>second item</li></$listTag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun appendToListFromTop() {
        safeAppend(editText, "not in list")
        safeAppend(editText, "\n")
        editText.toggleFormatting(listType)
        val mark = editText.length() - 1
        safeAppend(editText, "first item")
        safeAppend(editText, "\n")
        safeAppend(editText, "second item")

        editText.setSelection(mark)
        editText.text.insert(mark,"addition ")

        Assert.assertEquals("not in list<$listTag><li>addition first item</li><li>second item</li></$listTag>", editText.toHtml())
    }


    @Test
    @Throws(Exception::class)
    fun deleteFirstItemWithKeyboard() {
        editText.toggleFormatting(listType)
        safeAppend(editText, "first item")
        val firstMark = editText.length()
        safeAppend(editText, "\n")
        safeAppend(editText, "second item")
        val secondMark = editText.length()
        safeAppend(editText, "\n")
        safeAppend(editText, "third item")
        editText.setSelection(0)
        editText.text.delete(firstMark+1,secondMark)

        Assert.assertEquals("<$listTag><li>first item</li><li></li><li>third item</li></$listTag>", editText.toHtml())

        editText.text.delete(0,firstMark)

        Assert.assertEquals("<$listTag><li></li><li></li><li>third item</li></$listTag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun addMultipleEmptyItemsWithKeyboard() {
        editText.toggleFormatting(listType)
        safeAppend(editText, "item")
        editText.text.insert(0, "\n")
        Assert.assertEquals("<$listTag><li></li><li>item</li></$listTag>", editText.toHtml())

        editText.text.insert(1, "\n")
        editText.text.insert(2, "\n")
        Assert.assertEquals("<$listTag><li></li><li></li><li></li><li>item</li></$listTag>", editText.toHtml())

        safeAppend(editText, "\n")
        safeAppend(editText, "\n")
        Assert.assertEquals("<$listTag><li></li><li></li><li></li><li>item</li></$listTag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun closeListWithEmptyLineBelowIt() {
        editText.fromHtml("<$listTag><li>Ordered</li></$listTag><br>not in list")

        //remove newline after list (put cursor on newline after list and press backspace)
        val mark = editText.text.indexOf("Ordered") + "Ordered".length
        editText.text.delete(mark + 1, mark + 2) // +1 to cater for the item's endline
        Assert.assertEquals("<$listTag><li>Ordered</li></$listTag>not in list", editText.toHtml())

        //press enter twice after at the end of the list to add new item and then remove it and close list
        editText.setSelection(mark)
        editText.text.insert(mark, "\n")

        editText.setSelection(mark + 1)
        editText.text.insert(mark + 1, "\n")
        Assert.assertEquals("<$listTag><li>Ordered</li></$listTag><br>not in list", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun closeListWithTrailingEmptyItem() {
        editText.fromHtml("<$listTag><li>Ordered</li><li></li></$listTag>")

        //insert newline after empty list item to remove it and close the list (put cursor on empty list item and pres enter)
        val mark = editText.text.indexOf("Ordered") + "Ordered".length + 1 // must add 1 because of the newline at item end
        editText.setSelection(mark)
        editText.text.insert(editText.selectionEnd, "\n")

        Assert.assertEquals("<$listTag><li>Ordered</li></$listTag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun addLinebreaksAfterListWithEmptyItem() {
        editText.fromHtml("<$listTag><li>item</li><li></li></$listTag>after")

        editText.text.insert(editText.text.indexOf("after"), "\n")
        Assert.assertEquals("<$listTag><li>item</li><li></li></$listTag><br>after", editText.toHtml())

        editText.text.insert(editText.text.indexOf("after"), "\n")
        Assert.assertEquals("<$listTag><li>item</li><li></li></$listTag><br><br>after", editText.toHtml())

        editText.text.delete(editText.text.indexOf("after") - 1, editText.text.indexOf("after"))
        Assert.assertEquals("<$listTag><li>item</li><li></li></$listTag><br>after", editText.toHtml())

        editText.text.delete(editText.text.indexOf("after") - 1, editText.text.indexOf("after"))
        Assert.assertEquals("<$listTag><li>item</li><li></li></$listTag>after", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun addLinebreaksAfterListWithNonEmptyItem() {
        editText.fromHtml("<$listTag><li>item</li><li>item2</li></$listTag>after")

        editText.text.insert(editText.text.indexOf("after"), "\n")
        Assert.assertEquals("<$listTag><li>item</li><li>item2</li></$listTag><br>after", editText.toHtml())

        editText.text.insert(editText.text.indexOf("after"), "\n")
        Assert.assertEquals("<$listTag><li>item</li><li>item2</li></$listTag><br><br>after", editText.toHtml())

        editText.text.delete(editText.text.indexOf("after") - 1, editText.text.indexOf("after"))
        Assert.assertEquals("<$listTag><li>item</li><li>item2</li></$listTag><br>after", editText.toHtml())

        editText.text.delete(editText.text.indexOf("after") - 1, editText.text.indexOf("after"))
        Assert.assertEquals("<$listTag><li>item</li><li>item2</li></$listTag>after", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun deleteTextFromLastItem() {
        editText.fromHtml("<$listTag><li>item</li><li></li><li></li><li>item2</li></$listTag>after")

        val mark = editText.text.indexOf("item2")
        editText.text.delete(mark, mark + "item2".length)
        Assert.assertEquals("<$listTag><li>item</li><li></li><li></li><li></li></$listTag>after", editText.toHtml())

        editText.text.delete(mark -1, mark)
        Assert.assertEquals("<$listTag><li>item</li><li></li><li></li></$listTag>after", editText.toHtml())
    }


    @Test
    @Throws(Exception::class)
    fun checkIfEndOfTextMarkerAddedToLastEmptyListItem() {
        editText.fromHtml("before<$listTag><li>item</li><li></li><li></li></$listTag>")

        Assert.assertEquals(editText.text.last(), Constants.END_OF_BUFFER_MARKER)

        editText.fromHtml("<$listTag><li>item</li><li></li></$listTag>")

        Assert.assertEquals(editText.text.last(), Constants.END_OF_BUFFER_MARKER)
    }

    @Test
    @Throws(Exception::class)
    fun addTwoNewlinesAfterList() {
        editText.fromHtml("<$listTag><li>a</li><li>b</li></$listTag>")

        safeAppend(editText, "\n")
        safeAppend(editText, "\n")
        Assert.assertEquals("<$listTag><li>a</li><li>b</li></$listTag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun deleteEmptyListItemWithBackspace() {
        editText.fromHtml("<$listTag><li>a</li><li>b</li></$listTag>")

        safeAppend(editText, "\n")
        editText.text.delete(safeLength(editText) - 1, safeLength(editText))
        Assert.assertEquals("<$listTag><li>a</li><li>b</li></$listTag>", editText.toHtml())

        safeAppend(editText, "\n")
        safeAppend(editText, "\n")
        safeAppend(editText, "c")
        Assert.assertEquals("a\nb\nc", editText.text.toString())
        Assert.assertEquals("<$listTag><li>a</li><li>b</li></$listTag>c", editText.toHtml())

        editText.text.delete(safeLength(editText) - 1, safeLength(editText))
        Assert.assertEquals("<$listTag><li>a</li><li>b</li></$listTag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun appendTextToLastItemWithBacskpace() {
        editText.fromHtml("<$listTag><li>a</li><li>b</li></$listTag>c")

        val mark = editText.text.indexOf("c")
        editText.text.delete(mark - 1, mark)
        Assert.assertEquals("<$listTag><li>a</li><li>bc</li></$listTag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun appendTextToEmptyLastItemWithBacskpace() {
        editText.fromHtml("<$listTag><li>a</li><li></li><li></li></$listTag>c")

        var mark = editText.text.indexOf("c")
        editText.text.delete(mark - 1, mark)
        Assert.assertEquals("a\n\nc", editText.text.toString())
        Assert.assertEquals("<$listTag><li>a</li><li></li><li>c</li></$listTag>", editText.toHtml())

        mark = editText.text.indexOf("c")
        editText.text.delete(mark - 1, mark)
        Assert.assertEquals("a\nc", editText.text.toString())
        Assert.assertEquals("<$listTag><li>a</li><li>c</li></$listTag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun deleteSecondEmptyLineAndTestForZwjCharOnFirst() {
        editText.fromHtml("<$listTag><li></li><li></li></$listTag>")

        editText.text.delete(safeLength(editText) - 1, safeLength(editText))

        Assert.assertEquals(EndOfBufferMarkerAdder.ensureEndOfTextMarker(""), editText.text.toString())
        Assert.assertEquals("<$listTag><li></li></$listTag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun deleteLastItemFromList() {
        editText.fromHtml("<$listTag><li>a</li></$listTag>")

        editText.setSelection(editText.length())
        editText.text.delete(safeLength(editText) - 1, safeLength(editText))

        Assert.assertEquals(EndOfBufferMarkerAdder.ensureEndOfTextMarker(""), editText.text.toString())
        Assert.assertEquals("<$listTag><li></li></$listTag>", editText.toHtml())

        backspaceAt(editText, safeLength(editText))
        Assert.assertEquals(EndOfBufferMarkerAdder.ensureEndOfTextMarker(""), editText.text.toString())
    }

    @Test
    @Throws(Exception::class)
    fun deleteLastItemFromListWithTextAbove() {
        editText.fromHtml("abc<$listTag><li>a</li></$listTag>")

        editText.text.delete(safeLength(editText) - 1, safeLength(editText))

        Assert.assertEquals(EndOfBufferMarkerAdder.ensureEndOfTextMarker("abc\n"), editText.text.toString())
        Assert.assertEquals("abc<$listTag><li></li></$listTag>", editText.toHtml())

        editText.text.delete(safeLength(editText) - 1, safeLength(editText))
        Assert.assertEquals(EndOfBufferMarkerAdder.ensureEndOfTextMarker("abc"), editText.text.toString())
        Assert.assertEquals("abc", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun addTwoLinesThenDeleteTheList() {
        editText.fromHtml("<$listTag><li></li></$listTag>")

        safeAppend(editText, "a")
        safeAppend(editText, "\n")
        safeAppend(editText, "b")
        Assert.assertEquals("<$listTag><li>a</li><li>b</li></$listTag>", editText.toHtml())

        editText.text.delete(safeLength(editText) - 1, safeLength(editText))
        Assert.assertEquals("a\n" + Constants.ZWJ_CHAR, editText.text.toString())

        editText.text.delete(safeLength(editText) - 1, safeLength(editText)) // don't delete the end-of-text marker
        Assert.assertEquals("a", editText.text.toString())

        editText.text.delete(safeLength(editText) - 1, safeLength(editText))
        Assert.assertEquals(Constants.ZWJ_STRING, editText.text.toString())
        Assert.assertEquals("<$listTag><li></li></$listTag>", editText.toHtml())

        //Send key event since that's the way AztecText will remove the style when text is "empty" (only having the
        //  end-of-text marker
        backspaceAt(editText, safeLength(editText))
        Assert.assertEquals(EndOfBufferMarkerAdder.ensureEndOfTextMarker(""), editText.text.toString())
        Assert.assertEquals("", editText.toHtml())
    }

    fun backspaceAt(text: EditText, position: Int) {
        text.setSelection(position)

        // Send key event since that's the way AztecText will remove the style when text is "empty" (only having the
        //  end-of-text marker
        editText.dispatchKeyEvent(KeyEvent(0, 0, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL, 0))
        editText.dispatchKeyEvent(KeyEvent(0, 0, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL, 0))
    }

    fun safeLength(text: EditText): Int {
        return EndOfBufferMarkerAdder.safeLength(text)
    }

    fun safeAppend(editText: EditText, string: String) {
        editText.text.insert(EndOfBufferMarkerAdder.safeLength(editText), string)
    }

    fun safeEmpty(editText: EditText): Boolean {
        return editText.text.toString() == EndOfBufferMarkerAdder.ensureEndOfTextMarker("")
    }
}
