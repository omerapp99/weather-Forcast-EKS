from selenium import webdriver
from selenium.webdriver.firefox.options import Options
from selenium.webdriver.support.wait import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.common.by import By
import unittest
import os

class TestSelenium(unittest.TestCase):

    def setUp(self):
        # Configure Firefox options for headless mode
        options = Options()
        options.add_argument('-headless')
        
        # Use webdriver manager or the default geckodriver in the container
        self.browser = webdriver.Firefox(options=options)

    def tearDown(self):
        self.browser.quit()

    def test_location_check_positive(self):
        # Use the service name from docker-compose as the hostname
        self.browser.get('http://frontend:3000/')

        input_xpath = '//*[@id="name"]'
        button_xpath = "/html/body/div/div/header/div/button"

        WebDriverWait(self.browser, 10).until(EC.presence_of_element_located((By.XPATH, input_xpath)))
        inputElement = self.browser.find_element(By.XPATH, input_xpath)
        inputElement.send_keys("Holon")

        WebDriverWait(self.browser, 10).until(EC.presence_of_element_located((By.XPATH, button_xpath)))
        click_elem = self.browser.find_element(By.XPATH, button_xpath)
        click_elem.click()

        # Increase wait times and add more robust error handling
        WebDriverWait(self.browser, 20).until(
            EC.presence_of_element_located((By.XPATH, "/html/body/div/div/header/div/table"))
        )
        city_element = WebDriverWait(self.browser, 20).until(
            EC.presence_of_element_located((By.XPATH, "/html/body/div/div/header/div/p[1]"))
        )
        country_element = WebDriverWait(self.browser, 20).until(
            EC.presence_of_element_located((By.XPATH, "/html/body/div/div/header/div/p[2]"))
        )
        table_element = WebDriverWait(self.browser, 20).until(
            EC.presence_of_element_located((By.XPATH, "/html/body/div/div/header/div/table/thead/tr/th[2]"))
        )

        self.assertEqual(city_element.text, "City: Holon")
        self.assertEqual(country_element.text, "Country: Israel")
        self.assertEqual(table_element.text, "Day Temperature (°C)")

    # Similar modifications for other test methods...

if __name__ == "__main__":
    unittest.main()