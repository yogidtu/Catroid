# Catroid: An on-device visual programming system for Android devices
# Copyright (C) 2010-2013 The Catrobat Team
# (<http://developer.catrobat.org/credits>)
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU Affero General Public License as
# published by the Free Software Foundation, either version 3 of the
# License, or (at your option) any later version.
#
# An additional term exception under section 7 of the GNU Affero
# General Public License, version 3, is available at
# http://developer.catrobat.org/license_additional_term
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
# GNU Affero General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
Feature: Set size to brick
  
  A brick setting the size of an Object in percent

  Background: 
    Given I have a Program
    And this program has an Object 'Object'

  Scenario Outline: Set the size of an Object
    Given 'Object' has a Start script
    And this script has a Set size to <size> brick
    When I start the program
    And I wait until the program has stopped
    Then 'Object' should have the size <expected>

    Examples: 
      | size | expected |
      | 70   | 70       |
      | -1   | 0        |
