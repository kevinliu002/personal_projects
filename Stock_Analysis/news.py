import requests
import json
import time
import config
from bs4 import BeautifulSoup
from selenium import webdriver
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.chrome.options import Options
from azure.ai.textanalytics import TextAnalyticsClient
from azure.core.credentials import AzureKeyCredential

class news:
    def __init__(self,ticker):
        self.ticker = ticker
        self.headline = ""
        self.body = ""
        self.score = 0
    
    def get_finviz(self):

        ## Selenium & WebDriver set-up
        options = webdriver.ChromeOptions()
        options.add_argument('headless')

        driver = webdriver.Chrome(chrome_options=options)
        time.sleep(2)
        # Automated search for ticker.
        request_url = "https://finviz.com/"
        page = driver.get(request_url)
        time.sleep(5)
        driver.find_element_by_xpath("//input[@placeholder='Search ticker, company or profile']").send_keys(self.ticker)
        driver.find_element_by_xpath("//input[@placeholder='Search ticker, company or profile']").send_keys(Keys.RETURN)
        
        headlines = []

        ## Extract news table.
        soup = BeautifulSoup(driver.page_source,'html.parser')
        news_table = soup.find("table",class_="fullview-news-outer")
        news_iterator = news_table.find_all("div",class_="news-link-container")
        for i in news_iterator:
           
            headline = i.find("div",class_="news-link-left")
            for a in headline.find_all('a',href = True):
                if a.text:
                    link= a['href']

                    #Visit link and extract information
                    options.add_argument('headless')
                    driver_2 = webdriver.Chrome(chrome_options=options)
                    page_link = driver_2.get(link)
                    soup_link = BeautifulSoup(driver_2.page_source,'html.parser')
                    array_of_news = soup_link.find_all('p')
                    full_text = ""

                    for n in array_of_news:
                        try:
                            text = n.get_text()
                            full_text = full_text + text
                        except:
                            continue
            
            ## Put it all in a [ (tuple)]
            headline_text = headline.get_text()
            headlines.append((headline_text,link,full_text))
        return headlines
    
    def bing_news(self):
        array_of_news = []
        subscription_key = config.subscription_key
        search_term = "Microsoft"
        search_url = "https://api.cognitive.microsoft.com/bing/v7.0/news/search"
        
        headers = {"Ocp-Apim-Subscription-Key" : subscription_key}
        params  = {"q": search_term, "textDecorations": True, "textFormat": "HTML"} 
        
        response = requests.get(search_url, headers=headers, params=params)
        response.raise_for_status()
        search_results = response.json()['value']
        
        for i in search_results:
            headline_text =  BeautifulSoup(i['name'],'html.parser').get_text()
            link = i['url']
            description = i['description']
           

            #Visit link and extract information
            options = webdriver.ChromeOptions()
            options.add_argument('headless')
            driver = webdriver.Chrome(chrome_options=options)
            page_link = driver.get(link)
            soup_link = BeautifulSoup(driver.page_source,'html.parser')
            all_news = soup_link.find_all('p')
            full_text = description

            for n in all_news:
                try:
                    text = n.get_text()
                    full_text = full_text + text
                except:
                    continue
        
            news = (headline_text,link,full_text)
            array_of_news.append(news)
            break

        return array_of_news

    def analyize_headlines(self,news):
        list_of_sentiment = []
        running_score = 0

        key = "455e5b5d14c44467b401a8367194579c"
        endpoint = "https://sentiment-analysis-liu.cognitiveservices.azure.com/"
        ta_credential = AzureKeyCredential(key)
        text_analytics_client = TextAnalyticsClient(endpoint=endpoint, credential=ta_credential)
    
        for i in news:
            headline_text = [i[0]]
           

            ##Headline Sentiment
            response_headline = text_analytics_client.analyze_sentiment(documents=headline_text)[0]
            overall_sentiment, positve_score, neutral_score, negative_score = response_headline.sentiment,response_headline.confidence_scores.positive \
            ,response_headline.confidence_scores.neutral, response_headline.confidence_scores.negative
            list_of_sentiment.append((overall_sentiment,positve_score-negative_score))
            running_score = running_score + (positve_score - negative_score)
        
        return running_score/len(list_of_sentiment)

            

    def analyize_body(self,news):
        key = config.key
        endpoint = config.endpoint
        ta_credential = AzureKeyCredential(key)
        text_analytics_client = TextAnalyticsClient(endpoint=endpoint, credential=ta_credential)
        
        running_score = 0
        list_of_sentiment = []


        for i in news:
            body_text = [i[2]]

        ##Body Sentiment
            response_body = text_analytics_client.analyze_sentiment(documents=body_text)[0]
            overall_sentiment_body = response_body.sentiment
            positve_score_body,neutral_score_body,negative_score_body =response_body.confidence_scores.positive ,response_body.confidence_scores.neutral,response_body.confidence_scores.negative
            running_score = running_score + (positve_score_body - negative_score_body)
            list_of_sentiment.append((overall_sentiment_body,positve_score_body-negative_score_body))
        
        return running_score/len(list_of_sentiment)

    def analyize_key_phrases(self,news):
        for i in news:
            body_text = [i[2]]

            key = config.key
            endpoint = config.endpoint
            ta_credential = AzureKeyCredential(key)
            text_analytics_client = TextAnalyticsClient(endpoint=endpoint, credential=ta_credential)

            key_phrases =  text_analytics_client.extract_key_phrases(documents=body_text)[0]




    




if __name__ == "__main__":
    news = news("AMZN")
    bing = news.bing_news()
    #finviz = news.get_finviz()
    news.analyize_headlines(finviz)

 

