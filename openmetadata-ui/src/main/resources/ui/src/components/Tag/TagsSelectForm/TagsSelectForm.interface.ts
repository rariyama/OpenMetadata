/*
 *  Copyright 2023 Collate.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

import { SelectOption } from '../../../components/AsyncSelectList/AsyncSelectList.interface';
import { Paging } from '../../../generated/type/paging';

export type TagsSelectFormProps = {
  placeholder: string;
  defaultValue: string[];
  onChange?: (value: string[]) => void;
  onSubmit: (tags: string[]) => Promise<void>;
  onCancel: () => void;
  fetchApi: (
    search: string,
    page: number
  ) => Promise<{
    data: SelectOption[];
    paging: Paging;
  }>;
};
