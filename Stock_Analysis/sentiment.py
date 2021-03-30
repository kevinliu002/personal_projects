import nltk
import config
from azure.ai.textanalytics import TextAnalyticsClient
from azure.core.credentials import AzureKeyCredential
from nltk.sentiment.vader import SentimentIntensityAnalyzer


class sentiment:
    def __init__(self,news):
        self.news = news
        self.headline_score = 0
        self.body_score = 0

    def analyize_headlines(self):
    ## Analyize the news' article headlines and return their sentiment as an array
    
        list_of_sentiment = []
        running_score = 0

        key = config.key
        endpoint = "https://sentiment-analysis-liu.cognitiveservices.azure.com/"
        ta_credential = AzureKeyCredential(key)
        text_analytics_client = TextAnalyticsClient(endpoint=endpoint, credential=ta_credential)

        for i in self.news:
            headline_text = [i[0]]
            

            ##Headline Sentiment
            response_headline = text_analytics_client.analyze_sentiment(documents=headline_text)[0]
            overall_sentiment =  response_headline.sentiment
            positve_score = response_headline.confidence_scores.positive
            neutral_score = response_headline.confidence_scores.neutral
            negative_score = response_headline.confidence_scores.negative


            list_of_sentiment.append((overall_sentiment,positve_score-negative_score))
        return list_of_sentiment

    def analyize_body(self):
    ## Analyize the body headlines and return their sentiment as an array
        key = config.key
        endpoint = "https://sentiment-analysis-liu.cognitiveservices.azure.com/"
        ta_credential = AzureKeyCredential(key)
        text_analytics_client = TextAnalyticsClient(endpoint=endpoint, credential=ta_credential)
        
        running_score = 0
        list_of_sentiment = []


        for i in self.news:
            body_text = [i[2]]

        ##Body Sentiment
            response_body = text_analytics_client.analyze_sentiment(documents=body_text)[0]
            overall_sentiment =  response_body.sentiment
            positve_score = response_body.confidence_scores.positive
            neutral_score = response_body.confidence_scores.neutral
            negative_score = response_body.confidence_scores.negative
            list_of_sentiment.append((overall_sentiment,positve_score-negative_score))

        return list_of_sentiment


    def analyize_key_phrases(self,news):
        list_of_key_phrases_score = []
        sia = SentimentIntensityAnalyzer()
        
        for i in self.news:
            body_text = [i[2]]

            key = config.key
            endpoint = config.endpoint
            ta_credential = AzureKeyCredential(key)
            text_analytics_client = TextAnalyticsClient(endpoint=endpoint, credential=ta_credential)

            key_phrases =  text_analytics_client.extract_key_phrases(documents=body_text)[0]


            list_of_key_phrases_score.append(sia.polarity_score(key_phrases)['compound'])
        return list_of_key_phrases_score

        









