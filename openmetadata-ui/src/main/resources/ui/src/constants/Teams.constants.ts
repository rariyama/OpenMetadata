/*
 *  Copyright 2022 Collate.
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

import { t } from 'i18next';
import DraggableBodyRow from '../components/Team/TeamDetails/DraggableBodyRow';

export const DRAGGABLE_BODY_ROW = 'DraggableBodyRow';

export const TABLE_CONSTANTS = {
  body: {
    row: DraggableBodyRow,
  },
};

export enum SUBSCRIPTION_WEBHOOK {
  MS_TEAMS = 'msTeams',
  SLACK = 'slack',
  G_CHAT = 'gChat',
  GENERIC = 'generic',
}

export const SUBSCRIPTION_WEBHOOK_OPTIONS = [
  {
    label: t('label.ms-team-plural'),
    value: SUBSCRIPTION_WEBHOOK.MS_TEAMS,
  },
  {
    label: t('label.slack'),
    value: SUBSCRIPTION_WEBHOOK.SLACK,
  },
  {
    label: t('label.g-chat'),
    value: SUBSCRIPTION_WEBHOOK.G_CHAT,
  },
];
