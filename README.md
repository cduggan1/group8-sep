# SWENG Group 8

Group 8 Project. Details to follow.

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

## API Queries

A list of all valid queries and query parameters for the API, and an explanation of their function.

### Web Crawlers

/scrape - This query calls the webscraper class.

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

*propertyType=*
- ?Filter=Value where filter can be any of the following:
Brand	Site	Email	HasEnsuite	HasStudio	HasTwin	LowestPrice	HighestPrice	YearOpened	Distance_TCD(Walk)	Distance_TCD(Bike)	Distance_TCD(Public Transport)	Gym?	TV Room?	Study_Space?	Laundry_Room	Cinema_Room	Rooftop_Garden	Balcony	Dishwasher	Stovetop	Cafeteria	Sports_Hall	Fast_WiFi	District	Flatmates	Disability_Access	Other_Info


  
#### ID  

*/:index
- Returns Property at position :index of database.
  

#### ALL  

*/all
- Returns Full Database Query
  
### ADMIN QUERIES

*/admin/log*
  - Will Return logs if it is enabled



## License

[MIT](https://choosealicense.com/licenses/mit/)

MIT License

Copyright (c) [year] [fullname]

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
