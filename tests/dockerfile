# Use an official Python runtime as a base image
FROM python:3.10-slim

# Install necessary dependencies for Selenium and Firefox
RUN apt-get update && \
    apt-get install -y \
    wget \
    curl \
    gnupg2 \
    ca-certificates \
    unzip \
    firefox-esr \
    sudo \
    && rm -rf /var/lib/apt/lists/*

# Install geckodriver
RUN wget https://github.com/mozilla/geckodriver/releases/download/v0.32.2/geckodriver-v0.32.2-linux64.tar.gz -O /tmp/geckodriver.tar.gz && \
    tar -xvzf /tmp/geckodriver.tar.gz -C /usr/local/bin && \
    rm /tmp/geckodriver.tar.gz

RUN pip install selenium==4.10.0

# Set the working directory
WORKDIR /app

# Copy the test code
COPY . /app/

# Run the tests when the container starts
CMD ["python3", "-m", "unittest", "discover", "-s", "tests", "-p", "*.py"]
