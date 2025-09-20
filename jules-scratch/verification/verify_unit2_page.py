import asyncio
from playwright.async_api import async_playwright, expect

async def main():
    async with async_playwright() as p:
        browser = await p.chromium.launch(headless=True)
        page = await browser.new_page()

        # Navigate to the local HTML file
        await page.goto("file:///app/unit2.html")

        # Give the page a moment to ensure all styles are applied
        await page.wait_for_timeout(1000)

        # --- Test 1: Spelling Exercise ---
        # Find an input, fill it correctly and incorrectly, then click the check button
        spell_inputs = page.locator('.spell-input')
        await spell_inputs.nth(0).fill('ea') # Correct
        await spell_inputs.nth(1).fill('ee') # Incorrect
        await page.get_by_role('button', name='检查答案').click()

        # --- Test 2: Tick-or-Cross Quiz ---
        # Answer one correctly and one incorrectly
        await page.locator('#tick-cross-quiz .quiz-item').nth(0).get_by_role('button', name='✓').click() # Correct
        await page.locator('#tick-cross-quiz .quiz-item').nth(2).get_by_role('button', name='✓').click() # Incorrect

        # --- Test 3: Yes/No Quiz ---
        await page.locator('#yes-no-quiz .quiz-item').nth(0).get_by_role('button', name='Yes').click() # Correct

        # --- Test 4: Drag and Drop Game ---
        # A simple check to ensure the game board has loaded.
        await expect(page.locator('#dnd-game')).to_be_visible()
        await expect(page.locator('.dnd-draggable').first).to_be_visible()
        await expect(page.locator('.dnd-starter').first).to_be_visible()

        # Take a screenshot of the page after interactions
        await page.screenshot(path="jules-scratch/verification/verification.png", full_page=True)

        await browser.close()

if __name__ == "__main__":
    asyncio.run(main())
