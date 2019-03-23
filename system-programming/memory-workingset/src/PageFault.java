/* It is in this file, specifically the replacePage function that will
   be called by MemoryManagement when there is a page fault.  The 
   users of this program should rewrite PageFault to implement the 
   page replacement algorithm.
*/

  // This PageFault file is an example of the FIFO Page Replacement 
  // Algorithm as described in the Memory Management section.

import java.util.*;

public class PageFault {

  /**
   * The page replacement algorithm for the memory management sumulator.
   * This method gets called whenever a page needs to be replaced.
   * <p>
   * The page replacement algorithm implemented here is Working Set
   * The for loop locks current working set and chooses the page
   * with minimal last touch time that was not read or modified.
   * If it does not exists, chooses random (first in this implementation)
   * page with minimal last touch time.
   * <pre>
   *   Page page = ( Page ) mem.elementAt( oldestUnusedPage )
   * </pre>
   * This line brings the contents of the Page at oldestPage (a 
   * specified integer) from the mem vector into the page object.  
   * Next recall the contents of the target page, replacePageNum.  
   * Set the physical memory address of the page to be added equal 
   * to the page to be removed.
   * <pre>
   *   controlPanel.removePhysicalPage( oldestUnusedPage )
   * </pre>
   * Once a page is removed from memory it must also be reflected 
   * graphically.  This line does so by removing the physical page 
   * at the oldestPage value.  The page which will be added into 
   * memory must also be displayed through the addPhysicalPage 
   * function call.  One must also remember to reset the values of 
   * the page which has just been removed from memory.
   *
   * @param mem is the vector which contains the contents of the pages 
   *   in memory being simulated.  mem should be searched to find the 
   *   proper page to remove, and modified to reflect any changes.  
   * @param virtPageNum is the number of virtual pages in the 
   *   simulator (set in Kernel.java).  
   * @param replacePageNum is the requested page which caused the 
   *   page fault.  
   * @param controlPanel represents the graphical element of the 
   *   simulator, and allows one to modify the current display.
   */
  public static void replacePage ( Vector mem , int virtPageNum , int replacePageNum , ControlPanel controlPanel ) 
  {
    int oldestPage = -1;
    int oldestTime = 0;
    int oldestUnusedPage = -1;
    int oldestUnusedTime = 0;

    for(int pageIndex = 0; pageIndex < mem.size(); ++pageIndex) {
      Page page = (Page) mem.elementAt(pageIndex);

      // skip unreflected pages
      if (page.physical == -1) continue;

      // get preferred random page
      if (page.lastTouchTime > oldestTime) {
        oldestTime = page.lastTouchTime;
        oldestPage = pageIndex;
      }
      // get page to replace
      if (page.R == 0 && page.M == 0 && page.inMemTime > oldestUnusedTime) {
        oldestUnusedTime = page.inMemTime;
        oldestUnusedPage = pageIndex;
      }
    }
    if (oldestUnusedPage == -1) {
      oldestUnusedPage = oldestPage;
    }

    // get replacement pages
    Page page = ( Page ) mem.elementAt( oldestUnusedPage );
    Page next = ( Page ) mem.elementAt( replacePageNum );

    // log
    System.out.println(page);

    // make replacement
    controlPanel.removePhysicalPage( oldestUnusedPage );
    next.physical = page.physical;
    controlPanel.addPhysicalPage( next.physical , replacePageNum );
    page.inMemTime = 0;
    page.lastTouchTime = 0;
    page.R = 0;
    page.M = 0;
    page.physical = -1;
  }
}
