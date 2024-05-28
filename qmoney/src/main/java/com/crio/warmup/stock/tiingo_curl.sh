#!/bin/bash

# Replace YOUR_API_KEY with your actual Tiingo API key
API_KEY="f71d2aff7eda45260540e474df4faa986f285d6d"

# Fetch end-of-day data for GOOGL
curl -X GET "https://api.tiingo.com/tiingo/daily/GOOGL/prices?token=${f71d2aff7eda45260540e474df4faa986f285d6d}" -H "Content-Type: application/json"

# Fetch end-of-day data for AAPL
curl -X GET "https://api.tiingo.com/tiingo/daily/AAPL/prices?token=${f71d2aff7eda45260540e474df4faa986f285d6d}" -H "Content-Type: application/json"

# Fetch end-of-day data for MSFT
curl -X GET "https://api.tiingo.com/tiingo/daily/MSFT/prices?token=${f71d2aff7eda45260540e474df4faa986f285d6d}" -H "Content-Type: application/json"
