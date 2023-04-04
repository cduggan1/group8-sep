# SWENG Group 8

Group 8 Project.

A centralized platform providing seamless access to a vast database of student accommodations and webscraped listings through a REST API, allowing bots to retrieve accurate and up-to-date information for students.

Used in collaboration with Genesys Architect, deployed [here](https://sweng8tcd.com/).

## Authors

- Creagh Duggan, 2nd Year
- Diana Hrisovescu, 2nd Year
- Ellie Smith, 2nd Year
- Jacek Kaczmarek, 2nd Year
- James Doyle, 2nd Year
- Liam Zone, 2nd Year
- Orson O'Sullivan, 2nd Year
- Virag Varga, 3rd Year
- Brian Bredican, 3rd Year
- Zhongyuan Liu, 3rd Year

## Endpoints

A list of all valid queries and query parameters for the API, and an explanation of their function.

### Web Crawlers

The /scrape/* route builds a URL to be processed by a web scraper, retrieves the data and returns it to the client.

#### Daft  

*rentalPrice_to=* 
- Sets maximum acceptable price
  
*propertyType=*
- Sets desired property type. 
- Can be passed multiple times to select more than one type, or not at all to select all types.  
- Valid property types : 
  - apartments
  - studio-apartments
  - houses
  
*numBaths_from=*
- Sets the minimum number of bathrooms.
  
*numBeds_from=*
- Sets minimum number of beds
  
*leaseLength_from=*
- Sets minimum lease length in months (e.g. leaseLength_from=12 is a one year lease)
  
*facilities=*
- Takes a string from the user and parses out valid facilities.
- Valid facilities : 
  - alarm
  - cable-television
  - dishwasher
  - garden-patio-balcony
  - central-heating
  - internet
  - microwave
  - parking
  - smoking
  - serviced-property
  - dryer
  - wheelchair-access
  - washing-machine
     
*BER=*
 - Takes a string from the user, looks for properties with a BER rating matching, or better than, the rating contained in the string
 - In the case of the string not being a valid BER rating, an empty list of residences is returned.
 - In the case of the input being "Exempt", "SI_666" is passed to the web crawler instead, and all BER ratings are returned
 - Valid BER Ratings : 
    - Exempt
    - G
    - F
    - E2
    - E1
    - D2
    - D1
    - C3
    - C2
    - C1
    - B3
    - B2
    - B1
    - A3
    - A2
    - A1
    
    
### DATABASE QUERIES
## 

#### fullQuery  

The /fullQuery route filters a list of accommodations based on query parameters, converts the filtered list to a JSON formatted string and returns the string to the client. 

*propertyType=*
- ?Filter=Value where filter can be any of the following:
- Non Negotiables Filters
    - Brand	Site, Email, HasEnsuite, HasStudio, HasTwin, Disabilty_Access 
- Regular Filters
    - LowestPrice, YearOpened, Distance_TCD(Walk), Distance_TCD(Bike), Distance_TCD(Public_Transport), Gym TV_Room, Study_Space, Laundry_Room, Cinema_Room, Rooftop_Garden, Balcony, Dishwasher, Stovetop, Cafeteria, Sports_Hall, Fast_WiFi, District, Flatmates, Other_Info
- Unique Filter
    -  HighestPrice is a unique filter that acts as a budget filter. It checks if an accommodation's prices are below the value and then strikes accordingly. 
    -  1 strike if only the highest price is over, two if both the lowest and highest price are over.


*/:index
- Returns Property at position :index of database.
  
*/all
- Returns Full Database Query
  
  
### ADMIN QUERIES

*/admin/log*
  - Will Return logs if it is enabled

*/admin/csvUpdate*
  - Updates internal data structure representation of info.csv
  
*/s/\**
Extracts an abbreviation from the path and calls a method that retrieves a URL associated with the abbreviation.
It will then redirect to that URL.
URL Shortening method.

*/checkCity/\**
Checks if the extracted string appears in the cityData configuration file.
Purpose is to allow the bot to check if we are compatible with a city.

## License

[MIT](https://choosealicense.com/licenses/mit/)

MIT License

Copyright (c) 2023 group8-sep

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

## Installation

Build and run our app!

```bash
  java -jar group8-sep.jar
```
